package io.github.goods.dao.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.goods.dao.GoodsServiceDao;
import io.github.goods.mapper.GoodsMapper;
import io.github.goods.po.Goods;
import io.github.panxiaochao.redis.utils.RedissonUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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
        RLock rLock = RedissonUtil.INSTANCE().rLock("goods_lock:" + id);
        try {
            // 尝试枷锁，最多等5s，上锁10s后自动解锁
            boolean trySuccess = RedissonUtil.INSTANCE().tryLock(rLock, 5, 10, TimeUnit.SECONDS);
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
            RedissonUtil.INSTANCE().unLock(rLock);
        }
        return 0;
    }
}

