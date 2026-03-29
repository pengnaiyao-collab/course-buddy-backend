package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.AssignmentPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssignmentMapper extends BaseMapper<AssignmentPO> {

    @Select("SELECT * FROM assignments WHERE course_id = #{courseId} AND deleted_at IS NULL")
    IPage<AssignmentPO> findByCourseIdAndDeletedAtIsNull(Page<AssignmentPO> page, @Param("courseId") Long courseId);

    @Select("SELECT * FROM assignments WHERE course_id = #{courseId} AND deleted_at IS NULL")
    List<AssignmentPO> findByCourseIdAndDeletedAtIsNull(@Param("courseId") Long courseId);

    @Select("SELECT COUNT(*) FROM assignments WHERE course_id = #{courseId} AND deleted_at IS NULL")
    long countByCourseIdAndDeletedAtIsNull(@Param("courseId") Long courseId);
}
