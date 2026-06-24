package io.github.sync.dao;

import io.github.sync.po.School;

/**
 * <p>
 * 学校信息服务类.
 * </p>
 *
 * @author Lypxc
 * @since 2026-06-24
 */
public interface SchoolServiceDao {

	/**
	 * 保存学校信息
	 * @param school 学校信息
	 * @return 是否成功
	 */
	boolean save(School school);

	/**
	 * 更新学校信息
	 * @param school 学校信息
	 * @return 是否成功
	 */
	boolean update(School school);

}
