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
 * 年级信息持久化对象
 *
 * @author Lypxc
 * @since 2026-06-24
 */
@Getter
@Setter
@TableName("grade")
public class Grade {

	@TableId(value = "id", type = IdType.NONE)
	private Long id;

	/**
	 * 组织机构代码
	 */
	@TableField("org_id")
	private Long orgId;

	/**
	 * 年级代码
	 */
	@TableField("njdm")
	private String njdm;

	/**
	 * 年级名称
	 */
	@TableField("njmc")
	private String njmc;

	/**
	 * 校区代码
	 */
	@TableField("xqdm")
	private String xqdm;

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
