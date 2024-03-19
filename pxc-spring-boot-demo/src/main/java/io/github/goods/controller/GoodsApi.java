package io.github.goods.controller;

import io.github.goods.dao.GoodsServiceDao;
import io.github.panxiaochao.core.response.R;
import io.github.panxiaochao.core.utils.JacksonUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "", description = "Api")
@RequiredArgsConstructor
@RestController
@RequestMapping("/goods/v1/goods")
public class GoodsApi {

    private final GoodsServiceDao goodsServiceDao;

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
                    int result = goodsServiceDao.updateByPrimaryKeyStore(id);
                    if (result == 1) {
                        System.out.println("当前线程: " + Thread.currentThread().getName() + " 抢到啦");
                        requestMap.put(Thread.currentThread().getName(), "--- 抢到了 ---");
                    } else {
                        System.out.println("当前线程: " + Thread.currentThread().getName() + " 没有抢到");
                        requestMap.put(Thread.currentThread().getName(), "没有抢到");
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
