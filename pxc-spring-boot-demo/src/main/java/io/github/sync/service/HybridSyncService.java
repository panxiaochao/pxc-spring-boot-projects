package io.github.sync.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.panxiaochao.boot3.redis.utils.RedissonUtil;
import io.github.panxiaochao.boot3.utils.JacksonUtil;
import io.github.panxiaochao.boot3.utils.OkHttp3Util;
import io.github.sync.dao.CampusServiceDao;
import io.github.sync.dao.ClassServiceDao;
import io.github.sync.dao.GradeServiceDao;
import io.github.sync.dao.SchoolServiceDao;
import io.github.sync.mapper.CampusMapper;
import io.github.sync.mapper.ClassMapper;
import io.github.sync.mapper.GradeMapper;
import io.github.sync.mapper.SchoolMapper;
import io.github.sync.po.Campus;
import io.github.sync.po.Classes;
import io.github.sync.po.Grade;
import io.github.sync.po.School;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HybridSyncService {

	private static final String ACCESS_TOKEN_URL = "https://jyjzhfw.qiantang.gov.cn/oauth2.0/accessToken";

	private static final String SCHOOL_URL = "https://jyjzhfw.qiantang.gov.cn/tyba/open-api/school/list";

	private static final String CAMPUS_URL = "https://jyjzhfw.qiantang.gov.cn/tyba/open-api/school/campus/list";

	private static final String GRADE_URL = "https://jyjzhfw.qiantang.gov.cn/tyba/open-api/school/campus/grade/list";

	private static final String CLASS_URL = "https://jyjzhfw.qiantang.gov.cn/tyba/open-api/school/campus/grade/class/list";

	private static final String ACCESS_TOKEN_CACHE_KEY = "sync:access_token";

	private static final String LAST_SYNC_SEQUENCE_KEY = "sync:last_sequence";

	private static final String LAST_SYNC_TIME_KEY = "sync:last_sync_time";

	private static final String CAMPUS_SYNC_QUEUE_KEY = "sync:queue:campus";

	private static final String GRADE_SYNC_QUEUE_KEY = "sync:queue:grade";

	private static final String CLASS_SYNC_QUEUE_KEY = "sync:queue:class";

	private static final String TOKEN_REFRESH_LOCK_KEY = "sync:token:refresh:lock";

	private final SchoolServiceDao schoolService;

	private final SchoolMapper schoolMapper;

	private final CampusServiceDao campusService;

	private final CampusMapper campusMapper;

	private final GradeServiceDao gradeService;

	private final GradeMapper gradeMapper;

	private final ClassServiceDao classService;

	private final ClassMapper classMapper;

	private final RedissonClient redissonClient;

	// @Scheduled(cron = "0 0 2 * * ?")
	public void executeFullSync() {
		log.info("========== 开始执行全量同步任务 ==========");
		long startTime = System.currentTimeMillis();

		try {
			SyncResult result = fullSync();
			long costTime = System.currentTimeMillis() - startTime;

			log.info("========== 全量同步任务完成 ==========");
			log.info("学校: {} 条, 校区: {} 条, 年级: {} 条, 班级: {} 条, 耗时: {} ms", result.getSchoolCount(),
					result.getCampusCount(), result.getGradeCount(), result.getClassCount(), costTime);

			RedissonUtil.set(LAST_SYNC_TIME_KEY, LocalDateTime.now().toString(), Duration.ofDays(7));
		}
		catch (Exception e) {
			log.error("全量同步任务失败", e);
		}
	}

	// @Scheduled(fixedRate = 600000)
	public void executeIncrementalSync() {
		log.info("========== 开始执行增量同步任务 ==========");
		long startTime = System.currentTimeMillis();

		try {
			long lastSequence = getLastSyncSequence();
			log.info("上次同步序列号: {}", lastSequence);

			SyncResult result = incrementalSync(lastSequence);
			long costTime = System.currentTimeMillis() - startTime;

			log.info("========== 增量同步任务完成 ==========");
			log.info("学校: {} 条, 校区: {} 条, 年级: {} 条, 班级: {} 条, 耗时: {} ms", result.getSchoolCount(),
					result.getCampusCount(), result.getGradeCount(), result.getClassCount(), costTime);
		}
		catch (Exception e) {
			log.error("增量同步任务失败", e);
		}
	}

	public SyncResult fullSync() {
		log.info("【全量同步】开始同步学校数据");
		List<School> allSchools = fetchAllSchools();
		log.info("【全量同步】获取到学校数据: {} 条", allSchools.size());

		if (!allSchools.isEmpty()) {
			schoolMapper.insert(allSchools, 2000);
			log.info("【全量同步】学校数据批量存储完成");
		}

		enqueueCampusSyncTasks(allSchools);

		SyncResult result = new SyncResult();
		result.setSchoolCount(allSchools.size());
		result.setCampusCount(processAllCampusTasks());
		result.setGradeCount(processAllGradeTasks());
		result.setClassCount(processAllClassTasks());

		return result;
	}

	public SyncResult incrementalSync(long lastSequence) {
		log.info("【增量同步】开始同步，lastSequence: {}", lastSequence);
		List<School> changedSchools = fetchSchoolsBySequence(lastSequence);
		log.info("【增量同步】获取到变更学校数据: {} 条", changedSchools.size());

		if (changedSchools.isEmpty()) {
			return new SyncResult(0, 0, 0, 0);
		}

		SyncResult result = new SyncResult();
		result.setSchoolCount(changedSchools.size());

		for (School school : changedSchools) {
			try {
				schoolMapper.insertOrUpdate(school);
				syncSchoolHierarchy(school);
				updateLastSyncSequence(school.getLastSequence());
			}
			catch (Exception e) {
				log.error("【增量同步】同步学校失败: {}", school.getXxmc(), e);
			}
		}

		return result;
	}

	private List<School> fetchAllSchools() {
		long limit = 1000L;
		List<School> allSchools = new ArrayList<>();
		long lastSequence = 0;
		int page = 1;
		Map<String, Object> header = new HashMap<>();

		while (true) {
			log.info("【全量同步】正在获取第 {} 页数据, lastSequence={}", page, lastSequence);

			List<School> pageSchools = fetchSchoolPage(lastSequence, limit, header);

			if (pageSchools.isEmpty()) {
				log.info("【全量同步】第 {} 页返回空数据，分页获取结束", page);
				break;
			}

			allSchools.addAll(pageSchools);
			lastSequence += pageSchools.size();
			log.info("【全量同步】第 {} 页获取到 {} 条数据", page, pageSchools.size());

			if (pageSchools.size() < limit) {
				log.info("【全量同步】返回数据量({}) < limit({})，分页获取结束", pageSchools.size(), limit);
				break;
			}

			page++;
		}

		log.info("【全量同步】学校数据获取完成，共 {} 页, {} 条数据", page, allSchools.size());
		return allSchools;
	}

	private List<School> fetchSchoolPage(long lastSequence, long limit, Map<String, Object> header) {
		Map<String, Object> params = new HashMap<>();
		params.put("accessToken", getAccessTokenSafely());
		params.put("lastSequence", lastSequence);
		params.put("limit", limit);

		try {
			String result = OkHttp3Util.doGet(SCHOOL_URL, params, header);
			JsonNode jsonNode = JacksonUtil.objectMapper().readTree(result);

			int code = jsonNode.get("code").asInt();
			if (code != 200) {
				log.error("【全量同步】获取学校信息失败, code={}, message={}", code, jsonNode.get("message").asText());
				return Collections.emptyList();
			}

			JsonNode dataNode = jsonNode.get("data");
			return JacksonUtil.toBean(dataNode, new TypeReference<List<School>>() {
			});
		}
		catch (Exception e) {
			log.error("【全量同步】获取学校分页数据异常, lastSequence={}, limit={}", lastSequence, limit, e);
			return Collections.emptyList();
		}
	}

	private List<School> fetchSchoolsBySequence(long lastSequence) {
		Map<String, Object> params = new HashMap<>();
		params.put("accessToken", getAccessTokenSafely());
		params.put("lastSequence", lastSequence);
		params.put("limit", 1000);

		try {
			String result = OkHttp3Util.doGet(SCHOOL_URL, params, null);
			JsonNode jsonNode = JacksonUtil.objectMapper().readTree(result);

			int code = jsonNode.get("code").asInt();
			if (code != 200) {
				return Collections.emptyList();
			}

			JsonNode dataNode = jsonNode.get("data");
			return JacksonUtil.toBean(dataNode, new TypeReference<List<School>>() {
			});
		}
		catch (Exception e) {
			log.error("【增量同步】获取学校数据异常", e);
			return Collections.emptyList();
		}
	}

	private void enqueueCampusSyncTasks(List<School> schools) {
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(CAMPUS_SYNC_QUEUE_KEY);
		List<String> schoolIds = schools.stream().map(School::getId).map(String::valueOf).toList();

		try {
			queue.addAll(schoolIds);
			log.info("【队列】已将 {} 个学校的校区同步任务加入队列", schools.size());
		}
		catch (Exception e) {
			log.error("【队列】加入校区同步队列失败", e);
		}
	}

	@Scheduled(fixedDelay = 2000)
	public void consumeCampusQueue() {
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(CAMPUS_SYNC_QUEUE_KEY);

		try {
			String schoolId = queue.poll(2, TimeUnit.SECONDS);
			if (schoolId == null) {
				return;
			}

			log.debug("【队列消费】处理校区同步任务 - schoolId: {}", schoolId);
			List<Campus> campuses = fetchAndSaveCampus(Long.parseLong(schoolId));

			if (!campuses.isEmpty()) {
				enqueGradeSyncTasks(campuses);
			}

			log.debug("【队列消费】学校 {} 的校区同步完成, 获取到 {} 个校区", schoolId, campuses.size());
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("【队列消费】校区队列消费被中断", e);
		}
		catch (Exception e) {
			log.error("【队列消费】处理校区同步任务异常", e);
		}
	}

	private int processAllCampusTasks() {
		AtomicInteger campusCount = new AtomicInteger(0);
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(CAMPUS_SYNC_QUEUE_KEY);

		while (!queue.isEmpty()) {
			try {
				String schoolId = queue.poll(5, TimeUnit.SECONDS);
				if (schoolId == null) {
					break;
				}

				List<Campus> campuses = fetchAndSaveCampus(Long.parseLong(schoolId));
				campusCount.addAndGet(campuses.size());

				if (!campuses.isEmpty()) {
					enqueGradeSyncTasks(campuses);
				}
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
			catch (Exception e) {
				log.error("【全量同步】处理校区任务异常", e);
			}
		}

		return campusCount.get();
	}

	private List<Campus> fetchAndSaveCampus(Long schoolId) {
		Map<String, Object> params = new HashMap<>();
		params.put("accessToken", getAccessTokenSafely());
		params.put("schoolId", schoolId);

		try {
			String result = OkHttp3Util.doGet(CAMPUS_URL, params, null);
			JsonNode jsonNode = JacksonUtil.objectMapper().readTree(result);

			int code = jsonNode.get("code").asInt();
			if (code != 200) {
				log.error("获取学校 {} 的校区信息失败, code={}", schoolId, code);
				return Collections.emptyList();
			}

			JsonNode dataNode = jsonNode.get("data");
			List<Campus> campusList = JacksonUtil.toBean(dataNode, new TypeReference<List<Campus>>() {
			});

			if (campusList == null || campusList.isEmpty()) {
				return Collections.emptyList();
			}

			campusMapper.insertOrUpdate(campusList, 2000);
			return campusList;
		}
		catch (Exception e) {
			log.error("获取学校 {} 的校区信息异常", schoolId, e);
			return Collections.emptyList();
		}
	}

	private void enqueGradeSyncTasks(List<Campus> campuses) {
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(GRADE_SYNC_QUEUE_KEY);
		List<String> campusIds = campuses.stream().map(Campus::getId).map(String::valueOf).collect(Collectors.toList());

		try {
			queue.addAll(campusIds);
			log.debug("【队列】已将 {} 个校区的年级同步任务加入队列", campuses.size());
		}
		catch (Exception e) {
			log.error("【队列】加入年级同步队列失败", e);
		}
	}

	@Scheduled(fixedDelay = 2000)
	public void consumeGradeQueue() {
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(GRADE_SYNC_QUEUE_KEY);

		try {
			String campusId = queue.poll(2, TimeUnit.SECONDS);
			if (campusId == null) {
				return;
			}

			log.debug("【队列消费】处理年级同步任务 - campusId: {}", campusId);
			List<Grade> grades = fetchAndSaveGrade(Long.parseLong(campusId));

			if (!grades.isEmpty()) {
				enqueClassSyncTasks(grades);
			}

			log.debug("【队列消费】校区 {} 的年级同步完成, 获取到 {} 个年级", campusId, grades.size());
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("【队列消费】年级队列消费被中断", e);
		}
		catch (Exception e) {
			log.error("【队列消费】处理年级同步任务异常", e);
		}
	}

	private int processAllGradeTasks() {
		AtomicInteger gradeCount = new AtomicInteger(0);
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(GRADE_SYNC_QUEUE_KEY);

		while (!queue.isEmpty()) {
			try {
				String campusId = queue.poll(5, TimeUnit.SECONDS);
				if (campusId == null) {
					break;
				}

				List<Grade> grades = fetchAndSaveGrade(Long.parseLong(campusId));
				gradeCount.addAndGet(grades.size());

				if (!grades.isEmpty()) {
					enqueClassSyncTasks(grades);
				}
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
			catch (Exception e) {
				log.error("【全量同步】处理年级任务异常", e);
			}
		}

		return gradeCount.get();
	}

	private List<Grade> fetchAndSaveGrade(Long campusId) {
		Map<String, Object> params = new HashMap<>();
		params.put("accessToken", getAccessTokenSafely());
		params.put("campusId", campusId);

		try {
			String result = OkHttp3Util.doGet(GRADE_URL, params, null);
			JsonNode jsonNode = JacksonUtil.objectMapper().readTree(result);

			int code = jsonNode.get("code").asInt();
			if (code != 200) {
				log.error("获取校区 {} 的年级信息失败, code={}", campusId, code);
				return Collections.emptyList();
			}

			JsonNode dataNode = jsonNode.get("data");
			List<Grade> gradeList = JacksonUtil.toBean(dataNode, new TypeReference<List<Grade>>() {
			});

			if (gradeList == null || gradeList.isEmpty()) {
				return Collections.emptyList();
			}

			gradeMapper.insertOrUpdate(gradeList, 2000);
			return gradeList;
		}
		catch (Exception e) {
			log.error("获取校区 {} 的年级信息异常", campusId, e);
			return Collections.emptyList();
		}
	}

	private void enqueClassSyncTasks(List<Grade> grades) {
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(CLASS_SYNC_QUEUE_KEY);
		List<String> gradeIds = grades.stream().map(Grade::getId).map(String::valueOf).collect(Collectors.toList());

		try {
			queue.addAll(gradeIds);
			log.debug("【队列】已将 {} 个年级的班级同步任务加入队列", grades.size());
		}
		catch (Exception e) {
			log.error("【队列】加入班级同步队列失败", e);
		}
	}

	@Scheduled(fixedDelay = 2000)
	public void consumeClassQueue() {
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(CLASS_SYNC_QUEUE_KEY);

		try {
			String gradeId = queue.poll(2, TimeUnit.SECONDS);
			if (gradeId == null) {
				return;
			}

			log.debug("【队列消费】处理班级同步任务 - gradeId: {}", gradeId);
			List<Classes> classes = fetchAndSaveClass(Long.parseLong(gradeId));
			log.debug("【队列消费】年级 {} 的班级同步完成, 获取到 {} 个班级", gradeId, classes.size());
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("【队列消费】班级队列消费被中断", e);
		}
		catch (Exception e) {
			log.error("【队列消费】处理班级同步任务异常", e);
		}
	}

	private int processAllClassTasks() {
		AtomicInteger classCount = new AtomicInteger(0);
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(CLASS_SYNC_QUEUE_KEY);

		while (!queue.isEmpty()) {
			try {
				String gradeId = queue.poll(5, TimeUnit.SECONDS);
				if (gradeId == null) {
					break;
				}

				List<Classes> classes = fetchAndSaveClass(Long.parseLong(gradeId));
				classCount.addAndGet(classes.size());
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
			catch (Exception e) {
				log.error("【全量同步】处理班级任务异常", e);
			}
		}

		return classCount.get();
	}

	private List<Classes> fetchAndSaveClass(Long gradeId) {
		Map<String, Object> params = new HashMap<>();
		params.put("accessToken", getAccessTokenSafely());
		params.put("gradeId", gradeId);

		try {
			String result = OkHttp3Util.doGet(CLASS_URL, params, null);
			JsonNode jsonNode = JacksonUtil.objectMapper().readTree(result);

			int code = jsonNode.get("code").asInt();
			if (code != 200) {
				log.error("获取年级 {} 的班级信息失败, code={}", gradeId, code);
				return Collections.emptyList();
			}

			JsonNode dataNode = jsonNode.get("data");
			List<Classes> classList = JacksonUtil.toBean(dataNode, new TypeReference<List<Classes>>() {
			});

			if (classList == null || classList.isEmpty()) {
				return Collections.emptyList();
			}

			classMapper.insertOrUpdate(classList, 2000);
			return classList;
		}
		catch (Exception e) {
			log.error("获取年级 {} 的班级信息异常", gradeId, e);
			return Collections.emptyList();
		}
	}

	private void syncSchoolHierarchy(School school) {
		try {
			List<Campus> campuses = fetchAndSaveCampus(school.getId());
			log.info("【增量同步】学校 {} 的校区: {} 个", school.getXxmc(), campuses.size());

			for (Campus campus : campuses) {
				List<Grade> grades = fetchAndSaveGrade(campus.getId());
				log.info("【增量同步】校区 {} 的年级: {} 个", campus.getXqmc(), grades.size());

				for (Grade grade : grades) {
					List<Classes> classes = fetchAndSaveClass(grade.getId());
					log.info("【增量同步】年级 {} 的班级: {} 个", grade.getNjmc(), classes.size());
				}
			}
		}
		catch (Exception e) {
			log.error("【增量同步】同步学校层级数据失败: {}", school.getXxmc(), e);
		}
	}

	private String getAccessTokenSafely() {
		String token = RedissonUtil.get(ACCESS_TOKEN_CACHE_KEY);
		if (token != null && !token.isEmpty()) {
			return token;
		}

		RLock lock = redissonClient.getLock(TOKEN_REFRESH_LOCK_KEY);
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					token = RedissonUtil.get(ACCESS_TOKEN_CACHE_KEY);
					if (token != null && !token.isEmpty()) {
						return token;
					}

					token = fetchNewToken();
					return token;
				}
				finally {
					lock.unlock();
				}
			}
			else {
				throw new RuntimeException("获取token锁超时");
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("获取token锁被中断", e);
		}
	}

	private String fetchNewToken() {
		try {
			HttpUrl httpUrl = HttpUrl.parse(ACCESS_TOKEN_URL)
				.newBuilder()
				.addQueryParameter("grant_type", "client_credentials")
				.addQueryParameter("client_id", "R4MQeazU")
				.addQueryParameter("client_secret", "8869e77aeb1c290b3db53aa4ae1a3b5585c7f056")
				.build();
			String result = OkHttp3Util.doPost(httpUrl, null, null);

			JsonNode jsonNode = JacksonUtil.objectMapper().readTree(result);
			String accessToken = jsonNode.get("access_token").asText();
			int expiresIn = jsonNode.get("expires_in").asInt();

			int cacheSeconds = expiresIn - 300;
			if (cacheSeconds <= 0) {
				cacheSeconds = expiresIn;
			}

			RedissonUtil.set(ACCESS_TOKEN_CACHE_KEY, accessToken, Duration.ofSeconds(cacheSeconds));
			log.info("获取新的access_token成功, 缓存时间: {} 秒", cacheSeconds);

			return accessToken;
		}
		catch (Exception e) {
			throw new RuntimeException("获取访问凭证失败: " + e.getMessage(), e);
		}
	}

	private long getLastSyncSequence() {
		String sequence = RedissonUtil.get(LAST_SYNC_SEQUENCE_KEY);
		return sequence != null ? Long.parseLong(sequence) : 0L;
	}

	private void updateLastSyncSequence(long sequence) {
		RedissonUtil.set(LAST_SYNC_SEQUENCE_KEY, String.valueOf(sequence), Duration.ofDays(7));
	}

	public long getCampusQueueSize() {
		return redissonClient.getBlockingQueue(CAMPUS_SYNC_QUEUE_KEY).size();
	}

	public long getGradeQueueSize() {
		return redissonClient.getBlockingQueue(GRADE_SYNC_QUEUE_KEY).size();
	}

	public long getClassQueueSize() {
		return redissonClient.getBlockingQueue(CLASS_SYNC_QUEUE_KEY).size();
	}

	@Data
	public static class SyncResult {

		private int schoolCount;

		private int campusCount;

		private int gradeCount;

		private int classCount;

		public SyncResult() {
		}

		public SyncResult(int schoolCount, int campusCount, int gradeCount, int classCount) {
			this.schoolCount = schoolCount;
			this.campusCount = campusCount;
			this.gradeCount = gradeCount;
			this.classCount = classCount;
		}

	}

}
