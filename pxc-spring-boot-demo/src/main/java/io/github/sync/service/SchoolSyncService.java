package io.github.sync.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.panxiaochao.boot3.redis.utils.RedissonUtil;
import io.github.panxiaochao.boot3.utils.JacksonUtil;
import io.github.panxiaochao.boot3.utils.OkHttp3Util;
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

/**
 * <p>
 * 混合数据同步服务 负责学校、校区、年级、班级数据的同步，支持全量同步和增量同步两种模式 使用 Redis 阻塞队列实现异步分层同步，通过 Redis 分布式锁保证 token
 * 获取的线程安全.
 * </p>
 *
 * <p>
 * 同步架构： 1. 全量同步：按分页获取学校 → 队列异步获取校区 → 队列异步获取年级 → 队列异步获取班级 2. 增量同步：基于 lastSequence 获取变更数据 →
 * 逐校同步层级数据 3. Token 管理：使用 Redis 缓存，提前5分钟失效，分布式锁保证并发安全.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolSyncService {

	// ==================== API 接口地址 ====================
	/** 获取访问凭证接口 */
	private static final String ACCESS_TOKEN_URL = "https://jyjzhfw.qiantang.gov.cn/oauth2.0/accessToken";

	/** 获取学校列表接口 */
	private static final String SCHOOL_URL = "https://jyjzhfw.qiantang.gov.cn/tyba/open-api/school/list";

	/** 获取校区列表接口 */
	private static final String CAMPUS_URL = "https://jyjzhfw.qiantang.gov.cn/tyba/open-api/school/campus/list";

	/** 获取年级列表接口 */
	private static final String GRADE_URL = "https://jyjzhfw.qiantang.gov.cn/tyba/open-api/school/campus/grade/list";

	/** 获取班级列表接口 */
	private static final String CLASS_URL = "https://jyjzhfw.qiantang.gov.cn/tyba/open-api/school/campus/grade/class/list";

	// ==================== Redis 缓存 Key ====================
	/** 访问凭证缓存 Key */
	private static final String ACCESS_TOKEN_CACHE_KEY = "sync:access_token";

	/** 最后同步序列号缓存 Key */
	private static final String LAST_SYNC_SEQUENCE_KEY = "sync:school:last_sequence";

	/** 最后同步时间缓存 Key */
	private static final String LAST_SYNC_TIME_KEY = "sync:school:last_sync_time";

	// ==================== Redis 队列 Key ====================
	/** 校区同步队列 Key */
	private static final String CAMPUS_SYNC_QUEUE_KEY = "sync:queue:school:campus";

	/** 年级同步队列 Key */
	private static final String GRADE_SYNC_QUEUE_KEY = "sync:queue:school:grade";

	/** 班级同步队列 Key */
	private static final String CLASS_SYNC_QUEUE_KEY = "sync:queue:school:class";

	// ==================== 分布式锁 Key ====================
	/** Token 刷新分布式锁 Key */
	private static final String TOKEN_REFRESH_LOCK_KEY = "sync:token:refresh:lock";

	// ==================== 依赖注入 ====================

	/** 学校数据 Mapper */
	private final SchoolMapper schoolMapper;

	/** 校区数据 Mapper */
	private final CampusMapper campusMapper;

	/** 年级数据 Mapper */
	private final GradeMapper gradeMapper;

	/** 班级数据 Mapper */
	private final ClassMapper classMapper;

	/** Redisson 客户端，用于分布式锁和阻塞队列 */
	private final RedissonClient redissonClient;

	// @Scheduled(cron = "0 0 2 * * ?")
	/**
	 * 执行全量同步任务（定时任务，当前已注释） 每天凌晨2点执行，同步所有学校、校区、年级、班级数据
	 */
	public void executeFullSync() {
		log.info("========== 开始执行全量同步任务 ==========");
		long startTime = System.currentTimeMillis();

		try {
			// 执行全量同步逻辑
			SyncResult result = fullSync();
			long costTime = System.currentTimeMillis() - startTime;

			// 记录同步结果和耗时
			log.info("========== 全量同步任务完成 ==========");
			log.info("学校: {} 条, 校区: {} 条, 年级: {} 条, 班级: {} 条, 耗时: {} ms", result.getSchoolCount(),
					result.getCampusCount(), result.getGradeCount(), result.getClassCount(), costTime);

			// 记录最后同步时间到 Redis，缓存7天
			RedissonUtil.set(LAST_SYNC_TIME_KEY, LocalDateTime.now().toString(), Duration.ofDays(7));
		}
		catch (Exception e) {
			log.error("全量同步任务失败", e);
		}
	}

	// @Scheduled(fixedRate = 600000)
	/**
	 * 执行增量同步任务（定时任务，当前已注释） 每10分钟执行一次，基于 lastSequence 获取变更的学校数据并同步其层级数据
	 */
	public void executeIncrementalSync() {
		log.info("========== 开始执行增量同步任务 ==========");
		long startTime = System.currentTimeMillis();

		try {
			// 获取上次同步的序列号，用于增量拉取
			long lastSequence = getLastSyncSequence();
			log.info("上次同步序列号: {}", lastSequence);

			// 执行增量同步逻辑
			SyncResult result = incrementalSync(lastSequence);
			long costTime = System.currentTimeMillis() - startTime;

			// 记录同步结果和耗时
			log.info("========== 增量同步任务完成 ==========");
			log.info("学校: {} 条, 校区: {} 条, 年级: {} 条, 班级: {} 条, 耗时: {} ms", result.getSchoolCount(),
					result.getCampusCount(), result.getGradeCount(), result.getClassCount(), costTime);
		}
		catch (Exception e) {
			log.error("增量同步任务失败", e);
		}
	}

	/**
	 * 全量同步：分页获取所有学校数据，并通过队列异步同步校区、年级、班级 同步流程：学校(批量插入) → 校区队列 → 年级队列 → 班级队列
	 * @return 同步结果统计
	 */
	public SyncResult fullSync() {
		log.info("【全量同步】开始同步学校数据");
		// 分页获取所有学校数据
		List<School> allSchools = fetchAllSchools();
		log.info("【全量同步】获取到学校数据: {} 条", allSchools.size());

		// 批量插入学校数据到数据库
		if (!allSchools.isEmpty()) {
			schoolMapper.insert(allSchools, 2000);
			log.info("【全量同步】学校数据批量存储完成");
		}

		// 将所有学校ID加入校区同步队列
		enqueueCampusSyncTasks(allSchools);

		// 同步结果统计
		SyncResult result = new SyncResult();
		result.setSchoolCount(allSchools.size());
		// 处理校区同步任务（会触发年级队列）
		result.setCampusCount(processAllCampusTasks());
		// 处理年级同步任务（会触发班级队列）
		result.setGradeCount(processAllGradeTasks());
		// 处理班级同步任务
		result.setClassCount(processAllClassTasks());

		return result;
	}

	/**
	 * 增量同步：基于 lastSequence 获取变更的学校数据，并同步其完整的层级数据 每次同步后更新 lastSequence，确保下次只获取新增/变更的数据
	 * @param lastSequence 上次同步的序列号
	 * @return 同步结果统计
	 */
	public SyncResult incrementalSync(long lastSequence) {
		log.info("【增量同步】开始同步，lastSequence: {}", lastSequence);
		// 获取序列号之后变更的学校数据
		List<School> changedSchools = fetchSchoolsBySequence(lastSequence);
		log.info("【增量同步】获取到变更学校数据: {} 条", changedSchools.size());

		// 如果没有变更数据，直接返回
		if (changedSchools.isEmpty()) {
			return new SyncResult(0, 0, 0, 0);
		}

		SyncResult result = new SyncResult();
		result.setSchoolCount(changedSchools.size());

		// 逐个学校同步其完整的层级数据（校区、年级、班级）
		for (School school : changedSchools) {
			try {
				// 插入或更新学校数据
				schoolMapper.insertOrUpdate(school);
				// 同步学校的完整层级数据
				syncSchoolHierarchy(school);
				// 更新最后同步序列号
				updateLastSyncSequence(school.getLastSequence());
			}
			catch (Exception e) {
				log.error("【增量同步】同步学校失败: {}", school.getXxmc(), e);
			}
		}

		return result;
	}

	/**
	 * 分页获取所有学校数据 使用 lastSequence 作为游标，每次获取 limit 条数据，直到返回空或数据量小于 limit
	 * @return 所有学校数据列表
	 */
	private List<School> fetchAllSchools() {
		long limit = 1000L;
		List<School> allSchools = new ArrayList<>();
		long lastSequence = 0;
		int page = 1;
		Map<String, Object> header = new HashMap<>();

		// 循环分页获取数据
		while (true) {
			log.info("【全量同步】正在获取第 {} 页数据, lastSequence={}", page, lastSequence);

			// 获取当前页数据
			List<School> pageSchools = fetchSchoolPage(lastSequence, limit, header);

			// 如果返回空数据，结束分页
			if (pageSchools.isEmpty()) {
				log.info("【全量同步】第 {} 页返回空数据，分页获取结束", page);
				break;
			}

			// 累加数据
			allSchools.addAll(pageSchools);
			// 更新游标
			lastSequence += pageSchools.size();
			log.info("【全量同步】第 {} 页获取到 {} 条数据", page, pageSchools.size());

			// 如果返回数据量小于 limit，说明已是最后一页
			if (pageSchools.size() < limit) {
				log.info("【全量同步】返回数据量({}) < limit({})，分页获取结束", pageSchools.size(), limit);
				break;
			}

			page++;
		}

		log.info("【全量同步】学校数据获取完成，共 {} 页, {} 条数据", page, allSchools.size());
		return allSchools;
	}

	/**
	 * 获取学校分页数据
	 * @param lastSequence 游标序列号
	 * @param limit 每页数量
	 * @param header 请求头
	 * @return 学校数据列表
	 */
	private List<School> fetchSchoolPage(long lastSequence, long limit, Map<String, Object> header) {
		Map<String, Object> params = new HashMap<>();
		// 安全获取 access_token
		params.put("accessToken", getAccessTokenSafely());
		params.put("lastSequence", lastSequence);
		params.put("limit", limit);

		try {
			// 调用学校列表 API
			String result = OkHttp3Util.doGet(SCHOOL_URL, params, header);
			JsonNode jsonNode = JacksonUtil.objectMapper().readTree(result);

			// 检查响应码
			int code = jsonNode.get("code").asInt();
			if (code != 200) {
				log.error("【全量同步】获取学校信息失败, code={}, message={}", code, jsonNode.get("message").asText());
				return Collections.emptyList();
			}

			// 解析返回的学校数据
			JsonNode dataNode = jsonNode.get("data");
			return JacksonUtil.toBean(dataNode, new TypeReference<List<School>>() {
			});
		}
		catch (Exception e) {
			log.error("【全量同步】获取学校分页数据异常, lastSequence={}, limit={}", lastSequence, limit, e);
			return Collections.emptyList();
		}
	}

	/**
	 * 基于序列号获取变更的学校数据（增量同步用）
	 * @param lastSequence 上次同步的序列号
	 * @return 变更的学校数据列表
	 */
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

	/**
	 * 将学校ID列表加入校区同步队列 全量同步时使用，将所有学校ID批量加入队列，由消费者异步处理
	 * @param schools 学校列表
	 */
	private void enqueueCampusSyncTasks(List<School> schools) {
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(CAMPUS_SYNC_QUEUE_KEY);
		// 提取学校ID列表
		List<String> schoolIds = schools.stream().map(School::getId).map(String::valueOf).toList();

		try {
			// 批量加入队列
			queue.addAll(schoolIds);
			log.info("【队列】已将 {} 个学校的校区同步任务加入队列", schools.size());
		}
		catch (Exception e) {
			log.error("【队列】加入校区同步队列失败", e);
		}
	}

	/**
	 * 校区同步队列消费者（定时任务） 每2秒从队列中取出一个学校ID，获取并保存其校区数据，然后将校区ID加入年级同步队列
	 */
	@Scheduled(fixedDelay = 2000)
	public void consumeCampusQueue() {
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(CAMPUS_SYNC_QUEUE_KEY);

		try {
			// 从队列中阻塞获取学校ID（最多等待2秒）
			String schoolId = queue.poll(2, TimeUnit.SECONDS);
			if (schoolId == null) {
				return;
			}

			log.debug("【队列消费】处理校区同步任务 - schoolId: {}", schoolId);
			// 获取并保存校区数据
			List<Campus> campuses = fetchAndSaveCampus(Long.parseLong(schoolId));

			// 如果有校区数据，将校区ID加入年级同步队列
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

	/**
	 * 处理所有校区同步任务（全量同步用） 循环处理队列中的所有校区同步任务，直到队列为空
	 * @return 同步的校区总数
	 */
	private int processAllCampusTasks() {
		AtomicInteger campusCount = new AtomicInteger(0);
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(CAMPUS_SYNC_QUEUE_KEY);

		// 循环处理直到队列为空
		while (!queue.isEmpty()) {
			try {
				String schoolId = queue.poll(5, TimeUnit.SECONDS);
				if (schoolId == null) {
					break;
				}

				// 获取并保存校区数据
				List<Campus> campuses = fetchAndSaveCampus(Long.parseLong(schoolId));
				campusCount.addAndGet(campuses.size());

				// 如果有校区数据，将校区ID加入年级同步队列
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

	/**
	 * 获取并保存指定学校的校区数据
	 * @param schoolId 学校ID
	 * @return 校区数据列表
	 */
	private List<Campus> fetchAndSaveCampus(Long schoolId) {
		Map<String, Object> params = new HashMap<>();
		params.put("accessToken", getAccessTokenSafely());
		params.put("schoolId", schoolId);

		try {
			// 调用校区列表 API
			String result = OkHttp3Util.doGet(CAMPUS_URL, params, null);
			JsonNode jsonNode = JacksonUtil.objectMapper().readTree(result);

			// 检查响应码
			int code = jsonNode.get("code").asInt();
			if (code != 200) {
				log.error("获取学校 {} 的校区信息失败, code={}", schoolId, code);
				return Collections.emptyList();
			}

			// 解析校区数据
			JsonNode dataNode = jsonNode.get("data");
			List<Campus> campusList = JacksonUtil.toBean(dataNode, new TypeReference<List<Campus>>() {
			});

			if (campusList == null || campusList.isEmpty()) {
				return Collections.emptyList();
			}

			// 批量插入或更新校区数据
			campusMapper.insertOrUpdate(campusList, 2000);
			return campusList;
		}
		catch (Exception e) {
			log.error("获取学校 {} 的校区信息异常", schoolId, e);
			return Collections.emptyList();
		}
	}

	/**
	 * 将校区ID列表加入年级同步队列
	 * @param campuses 校区列表
	 */
	private void enqueGradeSyncTasks(List<Campus> campuses) {
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(GRADE_SYNC_QUEUE_KEY);
		// 提取校区ID列表
		List<String> campusIds = campuses.stream().map(Campus::getId).map(String::valueOf).collect(Collectors.toList());

		try {
			// 批量加入队列
			queue.addAll(campusIds);
			log.debug("【队列】已将 {} 个校区的年级同步任务加入队列", campuses.size());
		}
		catch (Exception e) {
			log.error("【队列】加入年级同步队列失败", e);
		}
	}

	/**
	 * 年级同步队列消费者（定时任务） 每2秒从队列中取出一个校区ID，获取并保存其年级数据，然后将年级ID加入班级同步队列
	 */
	@Scheduled(fixedDelay = 2000)
	public void consumeGradeQueue() {
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(GRADE_SYNC_QUEUE_KEY);

		try {
			// 从队列中阻塞获取校区ID（最多等待2秒）
			String campusId = queue.poll(2, TimeUnit.SECONDS);
			if (campusId == null) {
				return;
			}

			log.debug("【队列消费】处理年级同步任务 - campusId: {}", campusId);
			// 获取并保存年级数据
			List<Grade> grades = fetchAndSaveGrade(Long.parseLong(campusId));

			// 如果有年级数据，将年级ID加入班级同步队列
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

	/**
	 * 处理所有年级同步任务（全量同步用） 循环处理队列中的所有年级同步任务，直到队列为空
	 * @return 同步的年级总数
	 */
	private int processAllGradeTasks() {
		AtomicInteger gradeCount = new AtomicInteger(0);
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(GRADE_SYNC_QUEUE_KEY);

		// 循环处理直到队列为空
		while (!queue.isEmpty()) {
			try {
				String campusId = queue.poll(5, TimeUnit.SECONDS);
				if (campusId == null) {
					break;
				}

				// 获取并保存年级数据
				List<Grade> grades = fetchAndSaveGrade(Long.parseLong(campusId));
				gradeCount.addAndGet(grades.size());

				// 如果有年级数据，将年级ID加入班级同步队列
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

	/**
	 * 获取并保存指定校区的年级数据
	 * @param campusId 校区ID
	 * @return 年级数据列表
	 */
	private List<Grade> fetchAndSaveGrade(Long campusId) {
		Map<String, Object> params = new HashMap<>();
		params.put("accessToken", getAccessTokenSafely());
		params.put("campusId", campusId);

		try {
			// 调用年级列表 API
			String result = OkHttp3Util.doGet(GRADE_URL, params, null);
			JsonNode jsonNode = JacksonUtil.objectMapper().readTree(result);

			// 检查响应码
			int code = jsonNode.get("code").asInt();
			if (code != 200) {
				log.error("获取校区 {} 的年级信息失败, code={}", campusId, code);
				return Collections.emptyList();
			}

			// 解析年级数据
			JsonNode dataNode = jsonNode.get("data");
			List<Grade> gradeList = JacksonUtil.toBean(dataNode, new TypeReference<List<Grade>>() {
			});

			if (gradeList == null || gradeList.isEmpty()) {
				return Collections.emptyList();
			}

			// 批量插入或更新年级数据
			gradeMapper.insertOrUpdate(gradeList, 2000);
			return gradeList;
		}
		catch (Exception e) {
			log.error("获取校区 {} 的年级信息异常", campusId, e);
			return Collections.emptyList();
		}
	}

	/**
	 * 将年级ID列表加入班级同步队列
	 * @param grades 年级列表
	 */
	private void enqueClassSyncTasks(List<Grade> grades) {
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(CLASS_SYNC_QUEUE_KEY);
		// 提取年级ID列表
		List<String> gradeIds = grades.stream().map(Grade::getId).map(String::valueOf).collect(Collectors.toList());

		try {
			// 批量加入队列
			queue.addAll(gradeIds);
			log.debug("【队列】已将 {} 个年级的班级同步任务加入队列", grades.size());
		}
		catch (Exception e) {
			log.error("【队列】加入班级同步队列失败", e);
		}
	}

	/**
	 * 班级同步队列消费者（定时任务） 每2秒从队列中取出一个年级ID，获取并保存其班级数据
	 */
	@Scheduled(fixedDelay = 2000)
	public void consumeClassQueue() {
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(CLASS_SYNC_QUEUE_KEY);

		try {
			// 从队列中阻塞获取年级ID（最多等待2秒）
			String gradeId = queue.poll(2, TimeUnit.SECONDS);
			if (gradeId == null) {
				return;
			}

			log.debug("【队列消费】处理班级同步任务 - gradeId: {}", gradeId);
			// 获取并保存班级数据
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

	/**
	 * 处理所有班级同步任务（全量同步用） 循环处理队列中的所有班级同步任务，直到队列为空
	 * @return 同步的班级总数
	 */
	private int processAllClassTasks() {
		AtomicInteger classCount = new AtomicInteger(0);
		RBlockingQueue<String> queue = redissonClient.getBlockingQueue(CLASS_SYNC_QUEUE_KEY);

		// 循环处理直到队列为空
		while (!queue.isEmpty()) {
			try {
				String gradeId = queue.poll(5, TimeUnit.SECONDS);
				if (gradeId == null) {
					break;
				}

				// 获取并保存班级数据
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

	/**
	 * 获取并保存指定年级的班级数据
	 * @param gradeId 年级ID
	 * @return 班级数据列表
	 */
	private List<Classes> fetchAndSaveClass(Long gradeId) {
		Map<String, Object> params = new HashMap<>();
		params.put("accessToken", getAccessTokenSafely());
		params.put("gradeId", gradeId);

		try {
			// 调用班级列表 API
			String result = OkHttp3Util.doGet(CLASS_URL, params, null);
			JsonNode jsonNode = JacksonUtil.objectMapper().readTree(result);

			// 检查响应码
			int code = jsonNode.get("code").asInt();
			if (code != 200) {
				log.error("获取年级 {} 的班级信息失败, code={}", gradeId, code);
				return Collections.emptyList();
			}

			// 解析班级数据
			JsonNode dataNode = jsonNode.get("data");
			List<Classes> classList = JacksonUtil.toBean(dataNode, new TypeReference<List<Classes>>() {
			});

			if (classList == null || classList.isEmpty()) {
				return Collections.emptyList();
			}

			// 批量插入或更新班级数据
			classMapper.insertOrUpdate(classList, 2000);
			return classList;
		}
		catch (Exception e) {
			log.error("获取年级 {} 的班级信息异常", gradeId, e);
			return Collections.emptyList();
		}
	}

	/**
	 * 同步学校的完整层级数据（增量同步用） 按顺序同步：学校 → 校区 → 年级 → 班级
	 * @param school 学校对象
	 */
	private void syncSchoolHierarchy(School school) {
		try {
			// 同步校区数据
			List<Campus> campuses = fetchAndSaveCampus(school.getId());
			log.info("【增量同步】学校 {} 的校区: {} 个", school.getXxmc(), campuses.size());

			// 遍历校区，同步年级数据
			for (Campus campus : campuses) {
				List<Grade> grades = fetchAndSaveGrade(campus.getId());
				log.info("【增量同步】校区 {} 的年级: {} 个", campus.getXqmc(), grades.size());

				// 遍历年级，同步班级数据
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

	/**
	 * 安全获取 access_token 优先从 Redis 缓存中获取，如果缓存不存在或已过期，则使用分布式锁获取新 token 使用分布式锁避免并发请求导致重复获取
	 * token
	 * @return access_token
	 */
	private String getAccessTokenSafely() {
		// 先从缓存获取
		String token = RedissonUtil.get(ACCESS_TOKEN_CACHE_KEY);
		if (token != null && !token.isEmpty()) {
			return token;
		}

		// 缓存未命中，尝试获取分布式锁
		RLock lock = redissonClient.getLock(TOKEN_REFRESH_LOCK_KEY);
		try {
			// 尝试获取锁，最多等待10秒
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				try {
					// 双重检查：获取锁后再次检查缓存
					token = RedissonUtil.get(ACCESS_TOKEN_CACHE_KEY);
					if (token != null && !token.isEmpty()) {
						return token;
					}

					// 缓存仍未命中，获取新 token
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

	/**
	 * 获取新的 access_token 并缓存 缓存时间 = expires_in - 300秒（提前5分钟失效，避免边界问题）
	 * @return access_token
	 */
	private String fetchNewToken() {
		try {
			// 构建获取 token 的请求 URL
			HttpUrl httpUrl = HttpUrl.parse(ACCESS_TOKEN_URL)
				.newBuilder()
				.addQueryParameter("grant_type", "client_credentials")
				.addQueryParameter("client_id", "R4MQeazU")
				.addQueryParameter("client_secret", "8869e77aeb1c290b3db53aa4ae1a3b5585c7f056")
				.build();
			String result = OkHttp3Util.doPost(httpUrl, null, null);

			// 解析响应
			JsonNode jsonNode = JacksonUtil.objectMapper().readTree(result);
			String accessToken = jsonNode.get("access_token").asText();
			int expiresIn = jsonNode.get("expires_in").asInt();

			// 计算缓存时间：提前5分钟失效
			int cacheSeconds = expiresIn - 300;
			if (cacheSeconds <= 0) {
				cacheSeconds = expiresIn;
			}

			// 缓存 token
			RedissonUtil.set(ACCESS_TOKEN_CACHE_KEY, accessToken, Duration.ofSeconds(cacheSeconds));
			log.info("获取新的access_token成功, 缓存时间: {} 秒", cacheSeconds);

			return accessToken;
		}
		catch (Exception e) {
			throw new RuntimeException("获取访问凭证失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 获取上次同步的序列号
	 * @return 序列号，如果不存在则返回 0
	 */
	private long getLastSyncSequence() {
		String sequence = RedissonUtil.get(LAST_SYNC_SEQUENCE_KEY);
		return sequence != null ? Long.parseLong(sequence) : 0L;
	}

	/**
	 * 更新最后同步序列号
	 * @param sequence 序列号
	 */
	private void updateLastSyncSequence(long sequence) {
		RedissonUtil.set(LAST_SYNC_SEQUENCE_KEY, String.valueOf(sequence), Duration.ofDays(7));
	}

	/**
	 * 获取校区同步队列大小
	 * @return 队列中待处理的任务数
	 */
	public long getCampusQueueSize() {
		return redissonClient.getBlockingQueue(CAMPUS_SYNC_QUEUE_KEY).size();
	}

	/**
	 * 获取年级同步队列大小
	 * @return 队列中待处理的任务数
	 */
	public long getGradeQueueSize() {
		return redissonClient.getBlockingQueue(GRADE_SYNC_QUEUE_KEY).size();
	}

	/**
	 * 获取班级同步队列大小
	 * @return 队列中待处理的任务数
	 */
	public long getClassQueueSize() {
		return redissonClient.getBlockingQueue(CLASS_SYNC_QUEUE_KEY).size();
	}

	/**
	 * 同步结果统计类 记录各类数据的同步数量
	 */
	@Data
	public static class SyncResult {

		/** 同步的学校数量 */
		private int schoolCount;

		/** 同步的校区数量 */
		private int campusCount;

		/** 同步的年级数量 */
		private int gradeCount;

		/** 同步的班级数量 */
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
