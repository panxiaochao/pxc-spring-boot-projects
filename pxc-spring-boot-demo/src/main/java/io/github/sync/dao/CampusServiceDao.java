package io.github.sync.dao;

import io.github.sync.po.Campus;

/**
 * <p>
 * 校区信息服务类.
 * </p>
 *
 * @author Lypxc
 * @since 2026-06-24
 */
public interface CampusServiceDao {

	/**
	 * 保存校区信息
	 * @param campus 校区信息
	 * @return 是否成功
	 */
	boolean save(Campus campus);

	/**
	 * 更新校区信息
	 * @param campus 校区信息
	 * @return 是否成功
	 */
	boolean update(Campus campus);

}
