package io.github.goods.controller;

import io.github.goods.dao.GoodsServiceDao;
import io.github.goods.po.Goods;
import io.github.panxiaochao.boot3.core.response.R;
import io.github.panxiaochao.boot3.core.utils.JacksonUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>  前端控制器.</p>
 *
 * @author Lypxc
 * @since 2024-02-07
 */
@Tag(name = "", description = "goods")
@RequiredArgsConstructor
@RestController
@RequestMapping("/goods/v1/goods")
public class GoodsApi {

    private final GoodsServiceDao goodsServiceDao;

    private final RedissonClient redissonClient;

    @GetMapping("/initRedis")
    public R<Void> initGoodsRedis() {
        goodsServiceDao.initGoodsRedis();
        return R.ok();
    }

    @GetMapping("/one")
    public R<Goods> one(int activityId) {
        return R.ok(goodsServiceDao.one(activityId));
    }

    @GetMapping("/drawsByRedis")
    public R<Object> drawsByRedis(int activityId) {
        Goods goods = goodsServiceDao.drawsGoodsByRedis(activityId);
        if (goods == null) {
            return R.ok("没有抢到");
        }
        return R.ok(goods);
    }

    @GetMapping("/draws")
    public R<Object> draws(int activityId) {
        Goods goods = goodsServiceDao.drawsGoods(activityId);
        if (goods == null) {
            return R.ok("没有抢到");
        }
        return R.ok(goods);
    }

    @GetMapping("/test")
    public R<Goods> test() {
        return R.ok();
    }

    @GetMapping("/buy")
    public R<Void> buyGoods(int id) {
        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch open = new CountDownLatch(1);
        final CountDownLatch buyers = new CountDownLatch(threadCount);
        Map<String, String> requestMap = new HashMap<>();
        for (int i = 0; i < threadCount; i++) {
            //提交线程到线程池去执行
            executorService.submit(() -> {
                System.out.println("当前线程: " + Thread.currentThread().getName() + " 准备就绪");
                try {
                    //等待，线程就位，但是不运行
                    open.await();
                    System.out.println("当前线程: " + Thread.currentThread().getName() + " 开始请求, 时间: " + LocalDateTime.now());
                    //执行业务代码
                    long start = System.currentTimeMillis();
                    int result = goodsServiceDao.updateByPrimaryKeyStore(id);
                    long cost = System.currentTimeMillis() - start;
                    if (result == 1) {
                        System.out.println("当前线程: " + Thread.currentThread().getName() + " 抢到啦");
                        requestMap.put(Thread.currentThread().getName(), "--- 抢到了 ---" + " 耗时：" + cost);
                    } else {
                        System.out.println("当前线程: " + Thread.currentThread().getName() + " 没有抢到");
                        requestMap.put(Thread.currentThread().getName(), "没有抢到" + " 耗时：" + cost);
                    }
                    buyers.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        try {
            Thread.sleep(1000);
            open.countDown();
            System.out.println("\n等待完毕，当前线程: " + Thread.currentThread().getName() + " 开始抢货");
            buyers.await();
            System.out.println("\n汇总结果：");
            JacksonUtil.prettyPrint(requestMap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        return R.ok();
    }


}
