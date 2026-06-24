package io.github.sync.dao;

import io.github.sync.po.Unit;

/**
 * <p>
 * 单位信息服务类.
 * </p>
 *
 * @author Lypxc
 * @since 2026-06-24
 */
public interface UnitServiceDao {

	/**
	 * 保存单位信息
	 * @param unit 单位信息
	 * @return 是否成功
	 */
	boolean save(Unit unit);

	/**
	 * 更新单位信息
	 * @param unit 单位信息
	 * @return 是否成功
	 */
	boolean update(Unit unit);

}
