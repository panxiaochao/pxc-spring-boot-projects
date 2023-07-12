package io.github.test.controller;

import io.github.panxiaochao.core.response.R;
import io.github.panxiaochao.core.utils.SystemServerUtil;
import io.github.panxiaochao.core.utils.sysinfo.ServerInfo;
import io.github.panxiaochao.operate.log.core.annotation.OperateLog;
import io.github.panxiaochao.ratelimiter.annotation.RateLimiter;
import io.github.panxiaochao.redis.utils.RedissonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * {@code TestController}
 * <p>
 * description:
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

    @Operation(summary = "无参接口", description = "无参接口描述", method = "GET")
    @GetMapping("/get/pxc")
    @RateLimiter
    @OperateLog(title = "测试模块", description = "无参接口")
    // @RepeatSubmitLimiter
    public R<User> getUser() {
        User user = RedissonUtil.INSTANCE().get("user");
        if (Objects.isNull(user)) {
            user = new User();
            user.setUserName("潘骁超");
            user.setCreateDate(new Date());
            user.setCreateDateTime(LocalDateTime.now());
            RedissonUtil.INSTANCE().set("user", user, 60, TimeUnit.SECONDS);
        } else {
            LOGGER.info("user get from Redis !");
        }
        // int a = 1 / 0;
        return R.ok(user);
    }

    @Operation(summary = "无参接口", description = "无参接口描述", method = "GET")
    @GetMapping("/get/pxc/{id}")
    @RateLimiter(key = "#id", rateLimiterType = RateLimiter.RateLimiterType.SINGLE)
    public User getUser(@PathVariable String id, @RequestParam(required = false) String username) {
        User user = new User();
        user.setId(id);
        user.setUserName(username);
        user.setCreateDate(new Date());
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
        return R.ok(SystemServerUtil.INSTANCE().getServerInfo());
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
    }
}
