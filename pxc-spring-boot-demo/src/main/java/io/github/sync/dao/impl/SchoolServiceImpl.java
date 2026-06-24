package io.github.sync.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.sync.dao.SchoolServiceDao;
import io.github.sync.mapper.SchoolMapper;
import io.github.sync.po.School;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 学校信息服务实现类.
 * </p>
 *
 * @author Lypxc
 * @since 2026-06-24
 */
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl extends ServiceImpl<SchoolMapper, School> implements SchoolServiceDao {

	/**
	 * LOGGER SchoolServiceImpl.class
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SchoolServiceImpl.class);

	private final SchoolMapper schoolMapper;

	@Override
	public boolean save(School school) {
		LOGGER.info("保存学校信息: {}", school);
		return save(school);
	}

	@Override
	public boolean update(School school) {
		LOGGER.info("更新学校信息: {}", school);
		return updateById(school);
	}

}
