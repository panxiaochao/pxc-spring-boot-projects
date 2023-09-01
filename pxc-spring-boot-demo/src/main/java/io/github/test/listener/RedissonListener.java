package io.github.test.listener;

import io.github.panxiaochao.redis.utils.RedissonUtil;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * <p>Redisson 监听</p>
 *
 * @author Lypxc
 * @since 2023-08-30
 */
@Component
public class RedissonListener implements ApplicationRunner, Ordered {

    @Override
    public void run(ApplicationArguments args) {
        RedissonUtil.INSTANCE().subscribe("publishKey", String.class, (msg) -> {
            System.out.println("订阅通道 => publishKey, 接收值 => " + msg);
        });
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
