package io.github.sync.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.sync.dao.ClassServiceDao;
import io.github.sync.mapper.ClassMapper;
import io.github.sync.po.ClassEntity;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 班级信息服务实现类.
 * </p>
 *
 * @author Lypxc
 * @since 2026-06-24
 */
@Service
@RequiredArgsConstructor
public class ClassServiceImpl extends ServiceImpl<ClassMapper, ClassEntity> implements ClassServiceDao {

	/**
	 * LOGGER ClassServiceImpl.class
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassServiceImpl.class);

	private final ClassMapper classMapper;

	@Override
	public boolean save(ClassEntity classEntity) {
		LOGGER.info("保存班级信息: {}", classEntity);
		return save(classEntity);
	}

	@Override
	public boolean update(ClassEntity classEntity) {
		LOGGER.info("更新班级信息: {}", classEntity);
		return updateById(classEntity);
	}

}
