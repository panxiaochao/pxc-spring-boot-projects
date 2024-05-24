package io.github.goods.dao.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.goods.dao.GoodsServiceDao;
import io.github.goods.mapper.GoodsMapper;
import io.github.goods.po.Goods;
import io.github.panxiaochao.core.utils.ArithmeticUtil;
import io.github.panxiaochao.redis.utils.RedissonUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>  服务实现类. </p>
 *
 * @author Lypxc
 * @since 2024-02-07
 */
@Service
@RequiredArgsConstructor
public class GoodsServiceDaoImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsServiceDao {

    /**
     * LOGGER GoodsServiceDaoImpl.class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsServiceDaoImpl.class);

    private final GoodsMapper goodsMapper;

    private final RedissonClient redissonClient;

//    /**
//     * 1.常规方法，减库存-会出现超卖(问题)
//     *
//     * @return 1 成功 0 失败
//     */
//    @Override
//    @Transactional
//    public int updateByPrimaryKeyStore(Integer id) {
//        // 查一下商品库存
//        Goods goods = goodsMapper.selectById(id);
//        // 判断库存是否大于0
//        if (goods.getStore() > 0) {
//            // 库存大于0，可以减库存
//            int update = goodsMapper.update(new LambdaUpdateWrapper<>(Goods.class)
//                    .eq(Goods::getId, goods.getId())
//                    .set(Goods::getStore, goods.getStore() - 1)
//            );
//            if (update > 0) {
//                LOGGER.info("订单获取成功，还剩{}库存！", goods.getStore() - 1);
//                return 1;
//            } else {
//                LOGGER.info("减库存失败，不能下订单");
//            }
//        } else {
//            LOGGER.info("无库存，不能下订单");
//        }
//        return 0;
//    }

    /**
     * 2.减库存，基于乐观锁解决超卖问题，给数据库的表加一个version字段用于版本控制，只能抢1单
     *
     * @return 1 成功 0 失败
     */
//    @Override
//    @Transactional
//    public int updateByPrimaryKeyStore(Integer id) {
//        // 查一下商品库存
//        Goods goods = goodsMapper.selectById(id);
//        // 判断库存是否大于0
//        if (goods.getStore() > 0) {
//            int store = goods.getStore() - 1;
//            int version = goods.getVersion();
//            int update = goodsMapper.update(new LambdaUpdateWrapper<>(Goods.class)
//                    .eq(Goods::getId, goods.getId())
//                    .eq(Goods::getVersion, version)
//                    .set(Goods::getStore, store)
//                    .set(Goods::getVersion, version + 1)
//            );
//            if (update > 0) {
//                LOGGER.info("订单获取成功，还剩{}库存！", store);
//                return 1;
//            } else {
//                LOGGER.info("无库存，不能下订单");
//            }
//        }
//        //返回结果
//        return 0;
//    }

    /**
     * 3.减库存，使用synchronized
     *
     * @return 1 成功 0 失败
     */
//    @Override
//    public int updateByPrimaryKeyStore(Integer id) {
//        //加锁  DCL模式
//        synchronized (this) {
//            // 查一下商品库存
//            Goods goods = goodsMapper.selectById(id);
//            // 判断库存是否大于0
//            if (goods.getStore() > 0) {
//                int store = goods.getStore() - 1;
//                int update = goodsMapper.update(new LambdaUpdateWrapper<>(Goods.class)
//                        .eq(Goods::getId, goods.getId())
//                        .set(Goods::getStore, store)
//                );
//                if (update > 0) {
//                    System.out.println("减库存成功，可以下订单");
//                    return 1;
//                }
//            }
//        }
//        //返回结果
//        return 0;
//    }

    /**
     * 4.减库存，使用Redis分布式锁
     *
     * @return 1 成功 0 失败
     */
    @Override
    public int updateByPrimaryKeyStore(Integer id) {
        RLock rLock = RedissonUtil.rLock("goods_lock:" + id);
        try {
            // 尝试枷锁，最多等5s，上锁10s后自动解锁
            boolean trySuccess = RedissonUtil.tryLock(rLock, 5, 10, TimeUnit.SECONDS);
            if (trySuccess) {
                // 查一下商品库存
                Goods goods = goodsMapper.selectById(id);
                // 判断库存是否大于0
                if (goods.getStore() > 0) {
                    int store = goods.getStore() - 1;
                    int update = goodsMapper.update(new LambdaUpdateWrapper<>(Goods.class)
                            .eq(Goods::getId, goods.getId())
                            .set(Goods::getStore, store)
                    );
                    if (update > 0) {
                        System.out.println("减库存成功，可以下订单");
                        return 1;
                    } else {
                        LOGGER.info("无库存，不能下订单");
                    }
                }
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedissonUtil.unLock(rLock);
        }
        return 0;
    }

    @Override
    public void initGoodsRedis() {
        // goods_stock:[activityId]
        RMap<Integer, Goods> rMap = redissonClient.getMap("goods_stock:1");
        rMap.clear();
        List<Goods> goodsList = goodsMapper.selectList(new LambdaUpdateWrapper<Goods>().eq(Goods::getActivityId, 1));
        // 再存储
        goodsList.forEach(s -> rMap.put(s.getId(), s));
    }


    @Override
    public Goods drawsGoods(int activityId) {
        RLock rLock = RedissonUtil.rLock("goods_lock:" + activityId);
        Goods goods = null;
        try {
            // 尝试枷锁，最多等5s，上锁10s后自动解锁
            boolean trySuccess = RedissonUtil.tryLock(rLock, 3, 10, TimeUnit.SECONDS);
            if (trySuccess) {
                // 查一下商品库存
                List<Goods> goodsList = goodsMapper.selectList(new LambdaUpdateWrapper<Goods>().eq(Goods::getActivityId, activityId));
                goodsList = goodsList.stream().filter(f -> f.getStore() > 0).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(goodsList)) {
                    return goods;
                }
                goods = drawLottery(goodsList);
                // 判断库存是否大于0
                if (goods != null) {
                    int store = goods.getStore() - 1;
                    goodsMapper.update(new LambdaUpdateWrapper<>(Goods.class)
                            .eq(Goods::getId, goods.getId())
                            .set(Goods::getStore, store)
                    );
                    return goods;
                }
            } else {
                return goods;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedissonUtil.unLock(rLock);
        }
        return goods;
    }

    @Override
    public Goods drawsGoodsByRedis(int activityId) {
        RLock rLock = RedissonUtil.rLock("goods_lock:" + activityId);
        Goods goods = null;
        try {
            // 尝试枷锁，最多等5s，上锁10s后自动解锁
            boolean trySuccess = RedissonUtil.tryLock(rLock, 3, 10, TimeUnit.SECONDS);
            if (trySuccess) {
                // 查一下商品库存
                RMap<Integer, Goods> rMap = redissonClient.getMap("goods_stock:1");
                List<Goods> goodsList = rMap.values().stream().filter(v -> v.getStore() > 0).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(goodsList)) {
                    return goods;
                }
                goods = drawLottery(goodsList);
                // 判断库存是否大于0
                if (goods != null) {
                    int store = goods.getStore() - 1;
                    goods.setStore(store);
                    rMap.put(goods.getId(), goods);
                    return goods;
                }
            } else {
                return goods;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedissonUtil.unLock(rLock);
        }
        return goods;

    }

    // 抽奖方法
    public Goods drawLottery(List<Goods> goodsList) {
        List<Goods> pool = new ArrayList<>();
        for (Goods goods : goodsList) {
            int count = ArithmeticUtil.mul(goods.getProbability().toString(), "10").intValue();
            for (int i = 0; i < count; i++) {
                pool.add(goods);
            }
        }
        // 打乱奖品的顺序
        Collections.shuffle(pool);
        // 生成0到1之间的随机数
        int randomNum = RandomUtils.nextInt(0, pool.size() - 1);
        Goods goods = pool.get(randomNum);
        if (goods.getStore() > 0) {
            return goods;
        }
        return null;
    }
}

