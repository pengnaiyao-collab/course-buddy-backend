package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.AssignmentPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

import java.util.List;

/**
 * 作业映射器
 */
@Mapper
public interface AssignmentMapper extends BaseMapper<AssignmentPO> {

    @Select("SELECT * FROM assignments WHERE course_id = #{courseId} AND deleted_at IS NULL ORDER BY created_at DESC")
    IPage<AssignmentPO> findByCourseIdAndDeletedAtIsNull(Page<AssignmentPO> page, @Param("courseId") Long courseId);

    @Select("SELECT COUNT(*) FROM assignments WHERE course_id = #{courseId} AND deleted_at IS NULL")
    long countByCourseIdAndDeletedAtIsNull(@Param("courseId") Long courseId);
    
    @Select("SELECT * FROM assignments WHERE course_id = #{courseId} ORDER BY created_at DESC")
    List<AssignmentPO> findAllByCourseIdIncludingDeleted(@Param("courseId") Long courseId);

    @Update("UPDATE assignments SET deleted_at = #{deletedAt} WHERE id = #{id} AND deleted_at IS NULL")
    int markDeleted(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);
}

