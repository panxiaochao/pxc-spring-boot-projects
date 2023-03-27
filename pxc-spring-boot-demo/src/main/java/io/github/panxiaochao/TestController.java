package io.github.panxiaochao;

import io.github.panxiaochao.common.response.R;
import io.github.panxiaochao.file.storage.engine.FileStorageEngine;
import io.github.panxiaochao.file.storage.meta.FileMetadata;
import io.github.panxiaochao.threadpool.executor.ThreadPoolTaskManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    private final FileStorageEngine fileStorageEngine;

    @Operation(summary = "无参接口", description = "无参接口描述", method = "GET")
    @GetMapping("/get/pxc")
    public User getUser() {
        User user = new User();
        user.setUserName("潘骁超");
        user.setCreateDate(new Date());

        threadPoolTaskManager.execute(() -> {
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

    @Operation(summary = "上传文件接口", description = "上传文件接口描述", method = "POST")
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<FileMetadata> upload(
            @Parameter(description = "要上传的文件", required = true) @RequestPart("file") MultipartFile file) {
        FileMetadata fileMetadata = fileStorageEngine.upload(file);
        return R.ok(fileMetadata);
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
