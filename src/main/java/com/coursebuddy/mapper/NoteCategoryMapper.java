package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.NoteCategoryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 笔记分类映射器
 */
@Mapper
public interface NoteCategoryMapper extends BaseMapper<NoteCategoryPO> {

    @Select("SELECT * FROM note_categories WHERE user_id = #{userId} ORDER BY sort_order ASC")
    List<NoteCategoryPO> findByUserIdOrderBySortOrderAsc(@Param("userId") Long userId);

    @Select("SELECT * FROM note_categories WHERE id = #{id} AND user_id = #{userId}")
    Optional<NoteCategoryPO> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) > 0 FROM note_categories WHERE user_id = #{userId} AND name = #{name}")
    boolean existsByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);
}
