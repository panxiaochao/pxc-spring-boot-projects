package io.github.test.controller;

import io.github.panxiaochao.core.response.R;
import io.github.panxiaochao.redis.utils.RedissonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.redisson.api.GeoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>Redis Geo测试</p>
 *
 * @author Lypxc
 * @since 2023-09-13
 */
@Tag(name = "Geo测试", description = "Geo测试")
@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class RedisGeoApi {

    private final static Logger LOGGER = LoggerFactory.getLogger(RedisGeoApi.class);

    private static final String GEO_KEY = "geo_key:city:hangzhou";

    @Operation(summary = "上报地址", description = "上报地址", method = "POST")
    @PostMapping("/add")
    public R<Void> geoAdd(String name, Double lng, Double lat) {
        RedissonUtil.INSTANCE().geoAdd(GEO_KEY, lng, lat, name);
        return R.ok();
    }

    @Operation(summary = "获取两者之间距离", description = "获取两者之间距离", method = "GET")
    @GetMapping("/distance")
    public R<String> getDistance(String firstMember, String secondMember) {
        Double distance = RedissonUtil.INSTANCE().distance(GEO_KEY, firstMember, secondMember, GeoUnit.KILOMETERS);
        return R.ok(firstMember + "距离" + secondMember + "家：" + distance + "公里");
    }

    @Operation(summary = "获取成员的距离", description = "获取成员的距离", method = "GET")
    @GetMapping("/findPosition")
    public R<Object> findPosition(String member) {
        return R.ok(RedissonUtil.INSTANCE().position(GEO_KEY, member));
    }

    @Operation(summary = "查找附近的人", description = "查找附近的人", method = "GET")
    @GetMapping("/nearBy")
    public R<Object> nearBy(Double lng, Double lat) {
        return R.ok(RedissonUtil.INSTANCE().search(GEO_KEY, lng, lat, 5, GeoUnit.KILOMETERS, 5));
    }

}
