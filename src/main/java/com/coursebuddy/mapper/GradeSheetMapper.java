package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.GradeSheetPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface GradeSheetMapper extends BaseMapper<GradeSheetPO> {

    @Select("SELECT * FROM grade_sheets WHERE course_id = #{courseId} AND student_id = #{studentId}")
    Optional<GradeSheetPO> findByCourseIdAndStudentId(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

    @Select("SELECT * FROM grade_sheets WHERE course_id = #{courseId}")
    IPage<GradeSheetPO> findByCourseId(Page<GradeSheetPO> page, @Param("courseId") Long courseId);

    @Select("SELECT * FROM grade_sheets WHERE course_id = #{courseId}")
    List<GradeSheetPO> findByCourseId(@Param("courseId") Long courseId);
}
