package io.github.sync.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.sync.dao.UnitServiceDao;
import io.github.sync.mapper.UnitMapper;
import io.github.sync.po.Unit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 单位信息服务实现类.
 * </p>
 *
 * @author Lypxc
 * @since 2026-06-24
 */
@Service
@RequiredArgsConstructor
public class UnitServiceImpl extends ServiceImpl<UnitMapper, Unit> implements UnitServiceDao {

	/**
	 * LOGGER UnitServiceImpl.class
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(UnitServiceImpl.class);

	private final UnitMapper unitMapper;

	@Override
	public boolean save(Unit unit) {
		LOGGER.info("保存单位信息: {}", unit);
		return save(unit);
	}

	@Override
	public boolean update(Unit unit) {
		LOGGER.info("更新单位信息: {}", unit);
		return updateById(unit);
	}

}
