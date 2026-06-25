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
 * 学校信息持久化对象
 *
 * @author Lypxc
 * @since 2026-06-24
 */
@Getter
@Setter
@TableName("sync_school")
public class School {

	@TableId(value = "id", type = IdType.NONE)
	private Long id;

	/**
	 * 组织机构代码
	 */
	@TableField("org_id")
	private Long orgId;

	/**
	 * 学校标识码
	 */
	@TableField("xxbsm")
	private String xxbsm;

	/**
	 * 办学类型码
	 */
	@TableField("xxbxlxm")
	private String xxbxlxm;

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
	 * 学校地址
	 */
	@TableField("xxdz")
	private String xxdz;

	/**
	 * 学校简称
	 */
	@TableField("xxjc")
	private String xxjc;

	/**
	 * 学校简介
	 */
	@TableField("xxjj")
	private String xxjj;

	/**
	 * 学校性质
	 */
	@TableField("xxxzm")
	private String xxxzm;

	/**
	 * 学校状态
	 */
	@TableField("xxzt")
	private String xxzt;

	/**
	 * 建校年月
	 */
	@TableField("jxny")
	private String jxny;

	/**
	 * 联系电话
	 */
	@TableField("lxdh")
	private String lxdh;

	/**
	 * 电子信箱
	 */
	@TableField("dzxx")
	private String dzxx;

	/**
	 * 统一社会信用代码
	 */
	@TableField("tyshxydm")
	private String tyshxydm;

	/**
	 * 学校邮政编码
	 */
	@TableField("xxyzbm")
	private String xxyzbm;

	/**
	 * 学校主管部门码
	 */
	@TableField("xxzgbmm")
	private String xxzgbmm;

	/**
	 * 行政区划码
	 */
	@TableField("xzqhm")
	private String xzqhm;

	/**
	 * 主页地址
	 */
	@TableField("zydz")
	private String zydz;

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
