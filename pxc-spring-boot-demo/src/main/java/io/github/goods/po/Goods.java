package io.github.goods.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * <p>  持久化对象
 *
 * @author Lypxc
 * @since 2024-02-07
 */
@Getter
@Setter
@TableName("goods")
public class Goods {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 活动ID
     */
    @TableField("activity_id")
    private Integer activityId;

    /**
     * 商品名称
     */
    @TableField("good_name")
    private String goodName;

    /**
     * 中奖概率
     */
    @TableField("probability")
    private BigDecimal probability;

    /**
     * 库存
     */
    @TableField("store")
    private Integer store;

    /**
     * 悲观锁
     */
    @TableField("version")
    private Integer version;
}
