package io.github.sync.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.sync.dao.GradeServiceDao;
import io.github.sync.mapper.GradeMapper;
import io.github.sync.po.Grade;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 年级信息服务实现类.
 * </p>
 *
 * @author Lypxc
 * @since 2026-06-24
 */
@Service
@RequiredArgsConstructor
public class GradeServiceImpl extends ServiceImpl<GradeMapper, Grade> implements GradeServiceDao {

	/**
	 * LOGGER GradeServiceImpl.class
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GradeServiceImpl.class);

	private final GradeMapper gradeMapper;

	@Override
	public boolean save(Grade grade) {
		LOGGER.info("保存年级信息: {}", grade);
		return save(grade);
	}

	@Override
	public boolean update(Grade grade) {
		LOGGER.info("更新年级信息: {}", grade);
		return updateById(grade);
	}

}
