package io.github.sync.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 校区信息持久化对象
 *
 * @author Lypxc
 * @since 2026-06-24
 */
@Getter
@Setter
@TableName("sync_campus")
public class Campus {

	@TableId(value = "id", type = IdType.NONE)
	private Long id;

	/**
	 * 组织机构代码
	 */
	@TableField("org_id")
	private Long orgId;

	/**
	 * 校区代码
	 */
	@TableField("xqdm")
	private String xqdm;

	/**
	 * 校区名称
	 */
	@TableField("xqmc")
	private String xqmc;

	/**
	 * 校区码
	 */
	@TableField("xqm")
	private String xqm;

	/**
	 * 校区状态
	 */
	@TableField("xqzt")
	private String xqzt;

	/**
	 * 是否虚拟校区(0:否,1:是)
	 */
	@TableField("sfxnxq")
	private Integer sfxnxq;

	/**
	 * 是否主校区(0:主校,1:分校)
	 */
	@TableField("sfzxqm")
	private Integer sfzxqm;

	/**
	 * 学校代码
	 */
	@TableField("xxdm")
	private String xxdm;

	/**
	 * 学校名称
	 */
	@TableField("xxmc")
	private String xxmc;

	/**
	 * 省(自治区、直辖市)
	 */
	@TableField("szzqzxs")
	private String szzqzxs;

	/**
	 * 地(市、州)
	 */
	@TableField("dsz")
	private String dsz;

	/**
	 * 县(区、旗)
	 */
	@TableField("qqq")
	private String qqq;

	/**
	 * 乡(镇)
	 */
	@TableField("xz")
	private String xz;

	/**
	 * 社区
	 */
	@TableField("sq")
	private String sq;

	/**
	 * 经度
	 */
	@TableField("x")
	private String x;

	/**
	 * 纬度
	 */
	@TableField("y")
	private String y;

	/**
	 * 校区地址
	 */
	@TableField("xqdz")
	private String xqdz;

	/**
	 * 建校年月
	 */
	@TableField("jxny")
	private String jxny;

	/**
	 * 规划规模(班级数量)
	 */
	@TableField("ghgm_bj")
	private Integer ghgmBj;

	/**
	 * 行政区划码集合
	 */
	@TableField("xzqhm")
	private String xzqhm;

	/**
	 * 创建时间
	 */
	@TableField("create_time")
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	@TableField("update_time")
	private LocalDateTime updateTime;

}
