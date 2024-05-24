package io.github.goods.dao;

import io.github.goods.po.Goods;

/**
 * <p>  服务类. </p>
 *
 * @author Lypxc
 * @since 2024-02-07
 */
public interface GoodsServiceDao {
    int updateByPrimaryKeyStore(Integer id);

    Goods drawsGoods(int activityId);

    Goods drawsGoodsByRedis(int activityId);

    void initGoodsRedis();
}
