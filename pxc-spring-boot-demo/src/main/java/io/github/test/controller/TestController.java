package io.github.test.controller;

import io.github.panxiaochao.common.filemeta.FileMetadata;
import io.github.panxiaochao.common.response.R;
import io.github.panxiaochao.common.utils.LocalhostUtil;
import io.github.panxiaochao.common.utils.SpringContextUtil;
import io.github.panxiaochao.database.metadata.builder.DefaultRulesBuilder;
import io.github.panxiaochao.database.metadata.po.ITable;
import io.github.panxiaochao.database.metadata.tool.FastDbTool;
import io.github.panxiaochao.file.storage.engine.FileStorageEngine;
import io.github.panxiaochao.ip2region.meta.IpInfo;
import io.github.panxiaochao.ip2region.template.Ip2regionTemplate;
import io.github.panxiaochao.minio.template.MinioTemplate;
import io.github.panxiaochao.operate.log.annotation.OperateLog;
import io.github.panxiaochao.redis.annotation.AccessRateLimit;
import io.github.panxiaochao.redis.utils.RedissonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
// @OperateLog(name = "测试模块", description = "描述")
public class TestController {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestController.class);

    // private final ThreadPoolTaskManager threadPoolTaskManager;

    private final FileStorageEngine fileStorageEngine;

    private final Ip2regionTemplate ip2regionTemplate;

    private final MinioTemplate minioTemplate;

    private final RedissonClient redissonClient;


    @Operation(summary = "无参接口", description = "无参接口描述", method = "GET")
    @GetMapping("/get/pxc")
    // @OperateLog(description = "无参接口", module = "TEST")
    public User getUser() throws Exception {
        User user = new User();
        RedissonUtil.INST().tryLock("PXC:111", 0, 5, TimeUnit.SECONDS,
                () -> {
                    try {
                        Thread.sleep(3000);
                        user.setUserName("潘骁超");
                        user.setCreateDate(new Date());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                    System.out.println("tryLock error!");
                    user.setUserName("12312312321");
                }
        );
        // int a = 1/0;
        // threadPoolTaskManager.execute(() -> {
        //     LOGGER.info("threadPoolTaskManager !!!");
        // });
        return user;
    }

    @Operation(summary = "无参接口", description = "无参接口描述", method = "GET")
    @GetMapping("/get/pxc/{id}")
    @OperateLog(description = "path接口", name = "TEST")
    @AccessRateLimit
    public User getUser(@PathVariable String id, @RequestParam(required = false) String username) {
        User user = new User();
        user.setId(id);
        user.setUserName(username);
        user.setCreateDate(new Date());
        SpringContextUtil.getInstance().publishEvent(user);
        // threadPoolTaskManager.execute(() -> {
        //     LOGGER.info("threadPoolTaskManager !!!");
        // });
        return user;
    }

    @GetMapping(path = "/table")
    // @Operation(summary = "获取table", description = "获取schema", method = "GET")
    @Parameter(name = "tableName", description = "表名")
    @Parameter(name = "fillColumns", description = "是否加载数据库表字段")
    public R<List<ITable>> table(@RequestParam(required = false) String tableName,
                                 @RequestParam(required = false) boolean fillColumns) {
        FastDbTool fastDbTool = FastDbTool.create(
                "jdbc:mysql://localhost:3306/oauth2?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai",
                "root", "root123456");
        if (fillColumns) {
            fastDbTool.rulesBuilder(DefaultRulesBuilder.Builder::enableFillColumns);
        }
        if (StringUtils.hasText(tableName)) {
            ITable table = fastDbTool.queryTables(tableName);
            return R.ok(Collections.singletonList(table));
        } else {
            List<ITable> tables = fastDbTool.queryTables();
            return R.ok(tables);
        }
    }

    @GetMapping(path = "/ip")
    // @Operation(summary = "获取ip", description = "获取ip", method = "GET")
    public R<IpInfo> ip() {
        return R.ok(ip2regionTemplate.memorySearch(LocalhostUtil.getHostIp()));
    }

    @PostMapping(value = "/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // @OperateLog(description = "上传接口", module = "TEST", businessType = OperateLog.BusinessType.IMPORT)
    public R<String> uploadFile(@RequestPart("file") MultipartFile file) throws Exception {
        // minioTemplate.putObject(file.getInputStream(), file.getOriginalFilename());
        return R.ok();
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

    @Operation(summary = "上传文件接口", description = "上传文件接口描述", method = "POST")
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // @OperateLog(description = "上传接口", module = "TEST", businessType = OperateLog.BusinessType.IMPORT)
    public R<FileMetadata> upload(
            @RequestParam String username,
            @Parameter(description = "要上传的文件", required = true) @RequestPart("file") MultipartFile file) {
        FileMetadata fileMetadata = fileStorageEngine.upload(file);
        return R.ok(fileMetadata);
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
