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
 * 班级信息持久化对象
 *
 * @author Lypxc
 * @since 2026-06-24
 */
@Getter
@Setter
@TableName("class")
public class ClassEntity {

	@TableId(value = "id", type = IdType.NONE)
	private Long id;

	/**
	 * 组织机构代码
	 */
	@TableField("org_id")
	private Long orgId;

	/**
	 * 班级代码
	 */
	@TableField("bjdm")
	private String bjdm;

	/**
	 * 班号
	 */
	@TableField("bj")
	private String bj;

	/**
	 * 班级名称
	 */
	@TableField("bjmc")
	private String bjmc;

	/**
	 * 年级代码
	 */
	@TableField("njdm")
	private String njdm;

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
