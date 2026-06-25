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
 * 单位信息持久化对象
 *
 * @author Lypxc
 * @since 2026-06-24
 */
@Getter
@Setter
@TableName("sync_unit")
public class Unit {

	@TableId(value = "id", type = IdType.NONE)
	private Long id;

	/**
	 * 组织机构代码
	 */
	@TableField("org_id")
	private Long orgId;

	/**
	 * 单位代码
	 */
	@TableField("dwdm")
	private String dwdm;

	/**
	 * 单位名称
	 */
	@TableField("dwmc")
	private String dwmc;

	/**
	 * 单位英文名称
	 */
	@TableField("dwywmc")
	private String dwywmc;

	/**
	 * 单位地址
	 */
	@TableField("dwdz")
	private String dwdz;

	/**
	 * 单位邮政编码
	 */
	@TableField("dwyzbm")
	private String dwyzbm;

	/**
	 * 单位主管部门码
	 */
	@TableField("dwzgbmm")
	private String dwzgbmm;

	/**
	 * 单位简介
	 */
	@TableField("dwjj")
	private String dwjj;

	/**
	 * 统一社会信用代码
	 */
	@TableField("tyshxydm")
	private String tyshxydm;

	/**
	 * 行政区划码
	 */
	@TableField("xzqhm")
	private String xzqhm;

	/**
	 * 数据同步序列号
	 */
	@TableField("last_sequence")
	private Long lastSequence;

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
