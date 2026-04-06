package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.LessonPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 课时映射器
 */
@Mapper
public interface LessonMapper extends BaseMapper<LessonPO> {

    @Select("SELECT * FROM lessons WHERE course_id = #{courseId} AND deleted_at IS NULL ORDER BY lesson_order ASC")
    IPage<LessonPO> findByCourseIdAndDeletedAtIsNullOrderByLessonOrderAsc(Page<LessonPO> page, @Param("courseId") Long courseId);

    @Select("SELECT MAX(lesson_order) FROM lessons WHERE course_id = #{courseId}")
    Optional<Integer> findMaxLessonOrderByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT COUNT(*) FROM lessons WHERE course_id = #{courseId} AND deleted_at IS NULL")
    long countByCourseIdAndDeletedAtIsNull(@Param("courseId") Long courseId);

    @Select("SELECT COUNT(*) FROM lessons WHERE course_id = #{courseId} AND is_published = true AND deleted_at IS NULL")
    long countByCourseIdAndIsPublishedTrueAndDeletedAtIsNull(@Param("courseId") Long courseId);
}
