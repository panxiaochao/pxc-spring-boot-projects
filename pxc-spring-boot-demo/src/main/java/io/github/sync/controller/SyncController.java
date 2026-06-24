package io.github.sync.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.panxiaochao.boot3.common.response.R;
import io.github.panxiaochao.boot3.redis.utils.RedissonUtil;
import io.github.panxiaochao.boot3.utils.JacksonUtil;
import io.github.panxiaochao.boot3.utils.OkHttp3Util;
import io.github.sync.mapper.SchoolMapper;
import io.github.sync.po.School;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
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

	// 获取访问凭证
	private static final String accessTokenUrl = "https://jyjzhfw.qiantang.gov.cn/oauth2.0/accessToken";

	// 批量获取单位信息
	private static final String unit_url = "https://jyjzhfw.qiantang.gov.cn/tyba/open-api/unit/list";

	// 批量获取学校信息
	private static final String school_url = "https://jyjzhfw.qiantang.gov.cn/tyba/open-api/school/list";

	// Redis缓存Key
	private static final String ACCESS_TOKEN_CACHE_KEY = "sync:access_token";

	private final SchoolMapper schoolMapper;

	/**
	 * 获取访问凭证（带Redis缓存）
	 * @return 访问凭证信息
	 */
	@GetMapping("/getAccessToken")
	public R<String> getAccessToken() {
		return R.ok(accessToken());
	}

	private String accessToken() {
		try {
			// 1. 先从Redis缓存中获取
			String cachedToken = RedissonUtil.get(ACCESS_TOKEN_CACHE_KEY);
			if (cachedToken != null && !cachedToken.isEmpty()) {
				System.out.println("accessToken 读取缓存！");
				return cachedToken;
			}
			// 2. 缓存中没有，调用接口获取新的token
			HttpUrl httpUrl = HttpUrl.parse(accessTokenUrl)
				.newBuilder()
				.addQueryParameter("grant_type", "client_credentials")
				.addQueryParameter("client_id", "R4MQeazU")
				.addQueryParameter("client_secret", "8869e77aeb1c290b3db53aa4ae1a3b5585c7f056")
				.build();
			String result = OkHttp3Util.doPost(httpUrl, null, null);

			// 3. 解析返回结果
			JsonNode jsonNode = JacksonUtil.objectMapper().readTree(result);
			String accessToken = jsonNode.get("access_token").asText();
			int expiresIn = jsonNode.get("expires_in").asInt();

			// 4. 计算缓存时间（提前5分钟失效）
			int cacheSeconds = expiresIn - 300; // 减去300秒（5分钟）
			if (cacheSeconds <= 0) {
				cacheSeconds = expiresIn; // 如果expires_in小于5分钟，则使用原始值
			}

			// 5. 将token存入Redis，设置过期时间
			RedissonUtil.set(ACCESS_TOKEN_CACHE_KEY, accessToken, Duration.ofSeconds(cacheSeconds));

			return accessToken;
		}
		catch (Exception e) {
			throw new RuntimeException("获取访问凭证失败: " + e.getMessage());
		}
	}

	/**
	 * 批量获取单位信息（没有数据）
	 * @return 单位信息列表
	 */
	@GetMapping("/getUnit")
	public R<String> getUnit(@RequestParam(required = false, defaultValue = "1000") Long limit) {
		Map<String, Object> params = new HashMap<>();
		params.put("accessToken", accessToken());
		params.put("lastSequence", 0);
		params.put("limit", 100);

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
	 * 批量获取学校信息（带分页）
	 * @param limit 期望获取的数据量，0 < limit <= 1000，默认为1000
	 * @return 学校信息列表
	 */
	@GetMapping("/getSchool")
	public R<String> getSchool(@RequestParam(required = false, defaultValue = "1000") Long limit) {
		log.info("开始获取学校信息, limit={}", limit);
		// 校验limit参数
		if (limit <= 0 || limit > 1000) {
			log.warn("limit参数不合法: {}", limit);
			return R.fail("limit参数必须满足 0 < limit <= 1000");
		}

		List<School> allSchoolList = new java.util.ArrayList<>();
		long lastSequence = 0L;
		int page = 1;
		Map<String, Object> header = new HashMap<>();
		// header.put("X-App-Id", "1");

		try {
			while (true) {
				log.info("正在获取第 {} 页数据, lastSequence={}, limit={}", page, lastSequence, limit);
				// 构建请求参数
				Map<String, Object> params = new HashMap<>();
				params.put("accessToken", accessToken());
				params.put("lastSequence", lastSequence);
				params.put("limit", limit);

				// 发送请求
				String result = OkHttp3Util.doGet(school_url, params, header);
				JsonNode jsonNode = JacksonUtil.objectMapper().readTree(result);

				// 检查响应状态
				int code = jsonNode.get("code").asInt();
				if (code != 200) {
					String message = jsonNode.get("message").asText();
					log.error("获取学校信息失败, code={}, message={}", code, message);
					return R.fail("获取学校信息失败: " + message);
				}

				// 解析data数组并转换为School实体类列表
				JsonNode dataNode = jsonNode.get("data");
				List<School> schoolList = JacksonUtil.toBean(dataNode);

				// 如果返回的数据为空，说明已经是最后一页，退出循环
				if (schoolList == null || schoolList.isEmpty()) {
					log.info("第 {} 页返回空数据，分页获取结束", page);
					break;
				}

				log.info("第 {} 页获取到 {} 条数据", page, schoolList.size());
				// 累加数据
				allSchoolList.addAll(schoolList);

				// 更新lastSequence：当前lastSequence + 本次返回的数据量
				lastSequence = lastSequence + schoolList.size();

				// 如果返回的数据量小于limit，说明已经是最后一页
				if (schoolList.size() < limit) {
					log.info("第 {} 页返回数据量({}) < limit({})，分页获取结束", page, schoolList.size(), limit);
					break;
				}

				page++;
			}

			log.info("学校信息获取完成, 总共 {} 页, 共 {} 条数据", page, allSchoolList.size());
			// 批量数据存储
			if (!allSchoolList.isEmpty()) {
				log.info("开始批量存储 {} 条学校数据到数据库", allSchoolList.size());
				schoolMapper.insert(allSchoolList, 5000);
				log.info("学校数据存储完成");
			}

			return R.ok("已存储 " + allSchoolList.size() + " 条学校数据到数据库");
		}
		catch (Exception e) {
			log.error("获取学校信息异常", e);
			return R.fail("获取学校信息失败: " + e.getMessage());
		}
	}

}
