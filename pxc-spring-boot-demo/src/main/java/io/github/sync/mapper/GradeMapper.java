package io.github.sync.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.sync.po.Grade;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 年级信息持久化接口
 * </p>
 *
 * @author Lypxc
 * @since 2026-06-24
 */
@Mapper
public interface GradeMapper extends BaseMapper<Grade> {

}
