package io.github.sync.dao;

import io.github.sync.po.ClassEntity;

/**
 * <p>
 * 班级信息服务类.
 * </p>
 *
 * @author Lypxc
 * @since 2026-06-24
 */
public interface ClassServiceDao {

	/**
	 * 保存班级信息
	 * @param classEntity 班级信息
	 * @return 是否成功
	 */
	boolean save(ClassEntity classEntity);

	/**
	 * 更新班级信息
	 * @param classEntity 班级信息
	 * @return 是否成功
	 */
	boolean update(ClassEntity classEntity);

}
