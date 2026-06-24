package io.github.sync.dao;

import io.github.sync.po.Grade;

/**
 * <p>
 * 年级信息服务类.
 * </p>
 *
 * @author Lypxc
 * @since 2026-06-24
 */
public interface GradeServiceDao {

	/**
	 * 保存年级信息
	 * @param grade 年级信息
	 * @return 是否成功
	 */
	boolean save(Grade grade);

	/**
	 * 更新年级信息
	 * @param grade 年级信息
	 * @return 是否成功
	 */
	boolean update(Grade grade);

}
