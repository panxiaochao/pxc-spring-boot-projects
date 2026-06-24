package io.github.sync.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.sync.dao.CampusServiceDao;
import io.github.sync.mapper.CampusMapper;
import io.github.sync.po.Campus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 校区信息服务实现类.
 * </p>
 *
 * @author Lypxc
 * @since 2026-06-24
 */
@Service
@RequiredArgsConstructor
public class CampusServiceImpl extends ServiceImpl<CampusMapper, Campus> implements CampusServiceDao {

	/**
	 * LOGGER CampusServiceImpl.class
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CampusServiceImpl.class);

	private final CampusMapper campusMapper;

	@Override
	public boolean save(Campus campus) {
		LOGGER.info("保存校区信息: {}", campus);
		return save(campus);
	}

	@Override
	public boolean update(Campus campus) {
		LOGGER.info("更新校区信息: {}", campus);
		return updateById(campus);
	}

}
