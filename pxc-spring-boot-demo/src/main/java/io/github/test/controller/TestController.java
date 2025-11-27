package io.github.test.controller;

import io.github.panxiaochao.boot3.captcha.draw.ArithmeticCaptcha;
import io.github.panxiaochao.boot3.captcha.draw.CharacterCaptcha;
import io.github.panxiaochao.boot3.core.response.R;
import io.github.panxiaochao.boot3.core.utils.QRCodeUtil;
import io.github.panxiaochao.boot3.core.utils.StrUtil;
import io.github.panxiaochao.boot3.core.utils.SystemServerUtil;
import io.github.panxiaochao.boot3.core.utils.sysinfo.ServerInfo;
import io.github.panxiaochao.boot3.crypto.encrypt.AesBytesEncryptor;
import io.github.panxiaochao.boot3.crypto.encrypt.BytesEncryptor;
import io.github.panxiaochao.boot3.crypto.utils.Base64Util;
import io.github.panxiaochao.boot3.ip2region.core.Ip2regionClient;
import io.github.panxiaochao.boot3.ip2region.core.IpInfo;
import io.github.panxiaochao.boot3.operate.log.core.annotation.OperateLog;
import io.github.panxiaochao.boot3.ratelimiter.annotation.RateLimiter;
import io.github.panxiaochao.boot3.redis.utils.RedissonUtil;
import io.github.panxiaochao.boot3.sensitive.annotation.Sensitive;
import io.github.panxiaochao.boot3.sensitive.annotation.Translate;
import io.github.panxiaochao.boot3.sensitive.strategy.sensitive.SensitiveStrategy;
import io.github.panxiaochao.boot3.sensitive.strategy.translate.TranslateStrategy;
import io.github.test.handle.IdCard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * <p>
 * 测试Api
 * </p>
 *
 * @author Lypxc
 * @since 2022-11-25
 */
@Tag(name = "测试", description = "描述")
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

	private final static Logger LOGGER = LoggerFactory.getLogger(TestController.class);

	private final RedissonConnectionFactory connectionFactory;

	private final BytesEncryptor aes = new AesBytesEncryptor();

	private final Ip2regionClient ip2regionClient;

	@Operation(summary = "无参接口", description = "无参接口描述", method = "GET")
	@GetMapping("/get/pxc")
	@RateLimiter
	@OperateLog(title = "测试模块", description = "无参接口")
	// @RepeatSubmitLimiter
	public R<User> getUser() {
		User user = RedissonUtil.get("user");
		if (Objects.isNull(user)) {
			user = new User();
			user.setUserName("潘骁超");
			user.setCreateDate(new Date());
			user.setCreateDateTime(LocalDateTime.now());
			RedissonUtil.set("user", user, Duration.ofSeconds(60));
		}
		else {
			LOGGER.info("user get from Redis !");
		}
		// int a = 1 / 0;
		return R.ok(user);
	}

	@Operation(summary = "无参接口", description = "无参接口描述", method = "GET")
	@GetMapping("/get/pxc/{id}")
	// @RateLimiter(key = "#id", rateLimiterType = RateLimiter.RateLimiterType.SINGLE)
	// @Cacheable(cacheNames = "user", key = "#id")
	public User getUser(@PathVariable String id, @RequestParam(required = false) String username) {
		User user = new User();
		user.setId(id);
		user.setUserName(username);
		user.setCreateDate(new Date());
		user.setIdCard("210397198608215431");
		user.setPhone("17640125371");
		user.setAddress("北京市朝阳区某某四合院1203室");
		user.setEmail("17640125371@163.com");
		user.setBankCard("6226456952351452853");
		user.setState("1");
		return user;
	}

	/**
	 * 测试接口
	 */
	@Operation(summary = "有参接口", description = "有参接口描述", method = "POST")
	@PostMapping("/post/pxc")
	// @OperateLog(description = "有参接口", module = "TEST")
	public User postPxc(@RequestBody User user) {
		return user;
	}

	/**
	 * 服务信息接口
	 */
	@Operation(summary = "服务信息接口", description = "服务信息接口", method = "GET")
	@GetMapping("/get/server")
	public R<ServerInfo> serverInfo() {
		return R.ok(SystemServerUtil.getServerInfo());
	}

	/**
	 * Redis publish 发布通知
	 */
	@GetMapping("/redis/publish")
	public R<String> publish() {
		for (int i = 0; i < 10; i++) {
			RedissonUtil.publish("publishKey", "publish msg: " + LocalDateTime.now());
		}
		return R.ok();
	}

	/**
	 * Redis 缓存监控
	 */
	@GetMapping("/redis/cache")
	public R<Properties> redisCache() {
		RedisConnection connection = connectionFactory.getConnection();
		Properties commandStats = connection.info("commandstats");

		List<Map<String, String>> pieList = new ArrayList<>();
		if (commandStats != null) {
			commandStats.stringPropertyNames().forEach(key -> {
				Map<String, String> data = new HashMap<>(2);
				String property = commandStats.getProperty(key);
				data.put("name", StrUtil.removeStart(key, "cmdstat_"));
				data.put("value", StrUtil.substringBetween(property, "calls=", ",usec"));
				pieList.add(data);
			});
		}
		System.out.println(pieList);
		System.out.println(connection.dbSize());
		return R.ok(connection.info());
	}

	/**
	 * Redis 分页
	 */
	@GetMapping("/redis/page")
	public R<Map<String, String>> redisPage() {
		String key = "Auth-user:loginUser:online:*";
		Set<String> keySet = RedissonUtil.getKeysByPattern(key);
		String[] keys = keySet.stream().skip(0).limit(10).toArray(String[]::new);
		Map<String, String> map = RedissonUtil.get(keys);
		return R.ok(map);
	}

	/**
	 * Redis 分页
	 */
	// @GetMapping("/tomcat/metric")
	// public R<Map<String, Object>> tomcatMetric(String name) {
	// TomcatMetric tomcatMetric = new TomcatMetric();
	// return R.ok(tomcatMetric.getMetrics(name));
	// }

	/**
	 * 获取二维码
	 */
	@GetMapping("/qrcode")
	public ResponseEntity<byte[]> qrcode(String content) {
		ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.ok();
		// 设置ContentType的值 IMAGE_PNG在浏览器返回图片
		bodyBuilder.contentType(MediaType.IMAGE_PNG);
		// image content
		byte[] qrcodeBytes = QRCodeUtil.build(content).toBytes();
		return bodyBuilder.body(qrcodeBytes);
	}

	/**
	 * 加密
	 */
	@GetMapping("/encrypt")
	public R<String> encrypt() {
		String content = "123456";
		byte[] bytes = aes.encrypt(content.getBytes(StandardCharsets.UTF_8));
		return R.ok(Base64Util.encodeToString(bytes));
	}

	/**
	 * 解密
	 */
	@GetMapping("/decrypt")
	public R<String> decrypt(String content) {
		byte[] bytes = aes.decrypt(Base64Util.decodeFromString(content));
		return R.ok(new String(bytes));
	}

	/**
	 * 字符串验证码
	 */
	@GetMapping("/captcha")
	public void captcha(int width, int height, int codeLength, HttpServletResponse response) {
		CharacterCaptcha captcha = CharacterCaptcha.builder()
			.codeLength(codeLength)
			.width(width)
			.height(height)
			.interfereType(1)
			.build();
		try (ServletOutputStream out = response.getOutputStream()) {
			// 禁止服务器缓存
			response.setDateHeader("Expires", 0);
			// 设置标准的 HTTP/1.1 no-cache headers.
			response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
			// 设置IE扩展 HTTP/1.1 no-cache headers (use addHeader).
			response.addHeader("Cache-Control", "post-check=0, pre-check=0");
			// 设置标准 HTTP/1.0 不缓存图片
			response.setHeader("Pragma", "no-cache");
			// 返回一个 jpeg图片, 默认是text/html
			response.setContentType(captcha.getContentType());
			captcha.writeTo(out);
			out.flush();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 算术验证码
	 */
	@GetMapping("/arithmetic_captcha")
	public void arithmeticCaptcha(int width, int height, int numLength, HttpServletResponse response) {
		ArithmeticCaptcha captcha = ArithmeticCaptcha.builder()
			.numLength(numLength)
			.width(width)
			.height(height)
			.build();
		System.out.println(captcha.getCaptchaCode());
		try (ServletOutputStream out = response.getOutputStream()) {
			// 禁止服务器缓存
			response.setDateHeader("Expires", 0);
			// 设置标准的 HTTP/1.1 no-cache headers.
			response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
			// 设置IE扩展 HTTP/1.1 no-cache headers (use addHeader).
			response.addHeader("Cache-Control", "post-check=0, pre-check=0");
			// 设置标准 HTTP/1.0 不缓存图片
			response.setHeader("Pragma", "no-cache");
			// 返回一个 jpeg图片, 默认是text/html
			response.setContentType(captcha.getContentType());
			captcha.writeTo(out);
			out.flush();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@GetMapping("/ip/prase/{ip}")
	public R<IpInfo> ipParse(@PathVariable String ip) {
		IpInfo region = ip2regionClient.memorySearch(ip);
		return R.ok(region);
	}

	@Getter
	@Setter
	@ToString
	@Schema(name = "用户信息", description = "用户信息描述")
	public static class User {

		@Schema(description = "ID")
		private String id;

		@Schema(description = "用户名")
		private String userName;

		/**
		 * 身份证
		 */
		// @Sensitive(strategy = SensitiveStrategy.ID_CARD)
		@Sensitive(handler = IdCard.class)
		@Schema(description = "身份证")
		private String idCard;

		/**
		 * 电话
		 */
		@Sensitive(strategy = SensitiveStrategy.PHONE)
		@Schema(description = "电话")
		private String phone;

		/**
		 * 地址
		 */
		@Sensitive(strategy = SensitiveStrategy.ADDRESS)
		@Schema(description = "地址")
		private String address;

		/**
		 * 邮箱
		 */
		@Sensitive(strategy = SensitiveStrategy.EMAIL)
		@Schema(description = "邮箱")
		private String email;

		/**
		 * 银行卡
		 */
		@Sensitive(strategy = SensitiveStrategy.BANK_CARD)
		@Schema(description = "银行卡")
		private String bankCard;

		@Schema(description = "int-ID")
		private int intId;

		@Schema(description = "Integer-ID")
		private Integer integerId;

		@Schema(description = "long-ID")
		private long longId;

		@Schema(description = "Long-ID")
		private Long pLongId;

		@Schema(description = "float-ID")
		private float floatId;

		@Schema(description = "pFloatId-ID")
		private Float pFloatId;

		@Schema(description = "double-ID")
		private double doubleId;

		@Schema(description = "Double-ID")
		private Double pDoubleId;

		@Schema(description = "BigDecimal-ID")
		private BigDecimal bigDecimalId;

		@Schema(description = "Map对象")
		private Map<String, String> map;

		@Schema(description = "List用户列表")
		private List<User> list;

		@Schema(description = "Date时间")
		private Date createDate;

		@Schema(description = "LocalDateTime时间")
		private LocalDateTime createDateTime;

		@Translate(strategy = TranslateStrategy.BOOLEAN)
		private String state;

	}

}
