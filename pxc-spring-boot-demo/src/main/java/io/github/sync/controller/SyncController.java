package io.github.sync.controller;

import io.github.panxiaochao.boot3.common.response.R;
import io.github.panxiaochao.boot3.utils.JacksonUtil;
import io.github.panxiaochao.boot3.utils.OkHttp3Util;
import io.github.sync.service.SchoolSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 同步数据 - 暂时
 * </p>
 *
 * @author lypxc
 * @since 2026-06-24
 * @version 1.0
 */
@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
@Slf4j
public class SyncController {

	// 批量获取单位信息
	private static final String unit_url = "https://jyjzhfw.qiantang.gov.cn/tyba/open-api/unit/list";

	// 批量获取学校信息
	private static final String school_url = "https://jyjzhfw.qiantang.gov.cn/tyba/open-api/school/list";

	// 批量获取校区信息
	private static final String campus_url = "https://jyjzhfw.qiantang.gov.cn/tyba/open-api/school/campus/list";

	/** 获取年级列表接口 */
	private static final String GRADE_URL = "https://jyjzhfw.qiantang.gov.cn/tyba/open-api/school/campus/grade/list";

	/** 获取班级列表接口 */
	private static final String CLASS_URL = "https://jyjzhfw.qiantang.gov.cn/tyba/open-api/school/campus/grade/class/list";

	private final SchoolSyncService schoolSyncService;

	/**
	 * 测试 - 批量获取单位信息（没有数据）
	 * @return 单位信息列表
	 */
	@GetMapping("/getUnit")
	public R<String> getUnit(@RequestParam(required = false, defaultValue = "1000") Long limit) {
		Map<String, Object> params = new HashMap<>();
		params.put("accessToken", schoolSyncService.getAccessTokenSafely());
		params.put("lastSequence", 0);
		params.put("limit", limit);

		Map<String, Object> header = new HashMap<>();
		// header.put("X-App-Id", "2069266047173910529");

		try {
			String result = OkHttp3Util.doGet(unit_url, params, header);
			return R.ok(result);
		}
		catch (Exception e) {
			return R.fail("获取单位信息失败: " + e.getMessage());
		}
	}

	/**
	 * 测试 - 批量获取学校列表
	 * @param limit 页大小
	 * @return 学校列表
	 */
	@GetMapping("/getSchoolList")
	public R<String> getSchoolList(@RequestParam(required = false, defaultValue = "1000") Long limit) {
		Map<String, Object> params = new HashMap<>();
		params.put("accessToken", schoolSyncService.getAccessTokenSafely());
		params.put("lastSequence", 0);
		params.put("limit", limit);

		Map<String, Object> header = new HashMap<>();

		try {
			String result = OkHttp3Util.doGet(school_url, params, header);
			return R.ok(JacksonUtil.toMap(result));
		}
		catch (Exception e) {
			return R.fail("获取校区列表失败: " + e.getMessage());
		}
	}

	/**
	 * 测试 - 根据学校ID获取校区列表
	 * @param schoolId 学校ID
	 * @return 校区列表
	 */
	@GetMapping("/getCampusList")
	public R<String> getCampusList(@RequestParam Long schoolId) {
		Map<String, Object> params = new HashMap<>();
		params.put("accessToken", schoolSyncService.getAccessTokenSafely());
		params.put("schoolId", schoolId);

		Map<String, Object> header = new HashMap<>();

		try {
			String result = OkHttp3Util.doGet(campus_url, params, header);
			return R.ok(JacksonUtil.toMap(result));
		}
		catch (Exception e) {
			return R.fail("获取学校详情失败: " + e.getMessage());
		}
	}

	/**
	 * 测试 - 根据校区ID获取年级列表
	 * @param campusId 校区ID
	 * @return 年级列表
	 */
	@GetMapping("/getGradeList")
	public R<String> getGradeList(@RequestParam Long campusId) {
		Map<String, Object> params = new HashMap<>();
		params.put("accessToken", schoolSyncService.getAccessTokenSafely());
		params.put("campusId", campusId);

		Map<String, Object> header = new HashMap<>();

		try {
			String result = OkHttp3Util.doGet(GRADE_URL, params, header);
			return R.ok(JacksonUtil.toMap(result));
		}
		catch (Exception e) {
			return R.fail("获取年级列表失败: " + e.getMessage());
		}
	}

	/**
	 * 测试 - 根据年级ID获取班级列表
	 * @param gradeId 年级ID
	 * @return 班级列表
	 */
	@GetMapping("/getClassList")
	public R<String> getClassList(@RequestParam Long gradeId) {
		Map<String, Object> params = new HashMap<>();
		params.put("accessToken", schoolSyncService.getAccessTokenSafely());
		params.put("gradeId", gradeId);

		Map<String, Object> header = new HashMap<>();

		try {
			String result = OkHttp3Util.doGet(CLASS_URL, params, header);
			return R.ok(JacksonUtil.toMap(result));
		}
		catch (Exception e) {
			return R.fail("获取班级列表失败: " + e.getMessage());
		}
	}

	@GetMapping("/fullSync")
	public R<String> triggerFullSync() {
		log.info("手动触发全量同步");
		try {
			SchoolSyncService.SyncResult result = schoolSyncService.fullSync();
			return R.ok(String.format("全量同步完成 - 学校: %d 条, 校区: %d 条, 年级: %d 条, 班级: %d 条", result.getSchoolCount(),
					result.getCampusCount(), result.getGradeCount(), result.getClassCount()));
		}
		catch (Exception e) {
			log.error("手动全量同步失败", e);
			return R.fail("全量同步失败: " + e.getMessage());
		}
	}

	@GetMapping("/incrementalSync")
	public R<String> triggerIncrementalSync() {
		log.info("手动触发增量同步");
		try {
			long lastSequence = 0;
			SchoolSyncService.SyncResult result = schoolSyncService.incrementalSync(lastSequence);
			return R.ok(String.format("增量同步完成 - 学校: %d 条, 校区: %d 条, 年级: %d 条, 班级: %d 条", result.getSchoolCount(),
					result.getCampusCount(), result.getGradeCount(), result.getClassCount()));
		}
		catch (Exception e) {
			log.error("手动增量同步失败", e);
			return R.fail("增量同步失败: " + e.getMessage());
		}
	}

	@GetMapping("/queueStatus")
	public R<String> getQueueStatus() {
		try {
			long campusQueueSize = schoolSyncService.getCampusQueueSize();
			long gradeQueueSize = schoolSyncService.getGradeQueueSize();
			long classQueueSize = schoolSyncService.getClassQueueSize();

			String status = String.format("队列状态 - 校区: %d 个待处理, 年级: %d 个待处理, 班级: %d 个待处理", campusQueueSize,
					gradeQueueSize, classQueueSize);
			return R.ok(status);
		}
		catch (Exception e) {
			return R.fail("获取队列状态失败: " + e.getMessage());
		}
	}

}
