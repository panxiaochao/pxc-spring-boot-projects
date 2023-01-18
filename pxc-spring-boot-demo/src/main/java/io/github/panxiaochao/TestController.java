package io.github.panxiaochao;

import io.github.panxiaochao.threadpool.executor.ThreadPoolTaskManager;
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

/**
 * {@code TestController}
 * <p> description:
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

    private final ThreadPoolTaskManager threadPoolTaskManager;

    @Operation(summary = "无参接口", description = "无参接口描述", method = "GET")
    @GetMapping("/get/pxc")
    public User getUser() {
        User user = new User();
        user.setUserName("潘骁超");
        user.setCreateDate(new Date());

        threadPoolTaskManager.execute("无参接口", () -> {
            LOGGER.info("threadPoolTaskManager !!!");
        });
        return user;
    }

    /**
     * 测试接口
     *
     * @return
     */
    @Operation(summary = "有参接口", description = "有参接口描述", method = "POST")
    @PostMapping("/post/pxc")
    public User postPxc(@RequestBody User user) {
        return user;
    }

    @Getter
    @Setter
    @ToString
    @Schema(name = "用户信息", description = "用户信息")
    public static class User {
        @Schema(name = "userName", description = "用户名", title = "sss")
        private String userName;
        @Schema(name = "intId")
        private int intId;
        @Schema(name = "integerId")
        private Integer integerId;
        @Schema(name = "longId")
        private long longId;
        @Schema(name = "pLongId")
        private Long pLongId;
        @Schema(name = "floatId")
        private float floatId;
        @Schema(name = "pFloatId")
        private Float pFloatId;
        @Schema(name = "doubleId")
        private double doubleId;
        @Schema(name = "pDoubleId")
        private Double pDoubleId;
        @Schema(name = "bigDecimalId")
        private BigDecimal bigDecimalId;
        @Schema(name = "map")
        private Map<String, String> map;
        @Schema(name = "list")
        private List<User> list;
        @Schema(name = "createDate")
        private Date createDate;
        @Schema(name = "createDateTime")
        private LocalDateTime createDateTime;
    }
}
