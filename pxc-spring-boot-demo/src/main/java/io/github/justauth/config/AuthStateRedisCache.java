//package io.github.justauth.config;
//
//import io.github.panxiaochao.redis.utils.RedissonUtil;
//import me.zhyd.oauth.cache.AuthStateCache;
//import org.springframework.stereotype.Component;
//
//import java.time.Duration;
//
///**
// * <p>使用Redis自定义State缓存接口</p>
// *
// * @author Lypxc
// * @version 1.0
// * @since 2024-03-19
// */
//@Component
//public class AuthStateRedisCache implements AuthStateCache {
//
//    /**
//     * 默认过期半分钟，30s
//     */
//    private static final long EXPIRE = 30;
//
//    /**
//     * 存入缓存
//     *
//     * @param key   缓存key
//     * @param value 缓存内容
//     */
//    @Override
//    public void cache(String key, String value) {
//        RedissonUtil.INSTANCE().set(key, value, Duration.ofSeconds(EXPIRE));
//    }
//
//    /**
//     * 存入缓存
//     *
//     * @param key     缓存key
//     * @param value   缓存内容
//     * @param timeout 指定缓存过期时间（毫秒）
//     */
//    @Override
//    public void cache(String key, String value, long timeout) {
//        RedissonUtil.INSTANCE().set(key, value, Duration.ofSeconds(timeout));
//    }
//
//    /**
//     * 获取缓存内容
//     *
//     * @param key 缓存key
//     * @return 缓存内容
//     */
//    @Override
//    public String get(String key) {
//        return RedissonUtil.INSTANCE().get(key);
//    }
//
//    /**
//     * 是否存在key，如果对应key的value值已过期，也返回false
//     *
//     * @param key 缓存key
//     * @return true：存在key，并且value没过期；false：key不存在或者已过期
//     */
//    @Override
//    public boolean containsKey(String key) {
//        return RedissonUtil.INSTANCE().isExists(key);
//    }
//}
