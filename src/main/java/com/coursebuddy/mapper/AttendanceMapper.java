package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.AttendancePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface AttendanceMapper extends BaseMapper<AttendancePO> {

    @Select("SELECT * FROM attendances WHERE course_id = #{courseId} AND student_id = #{studentId}")
    IPage<AttendancePO> findByCourseIdAndStudentId(Page<AttendancePO> page, @Param("courseId") Long courseId, @Param("studentId") Long studentId);

    @Select("SELECT * FROM attendances WHERE course_id = #{courseId}")
    IPage<AttendancePO> findByCourseId(Page<AttendancePO> page, @Param("courseId") Long courseId);

    @Select("SELECT * FROM attendances WHERE course_id = #{courseId} AND session_date = #{sessionDate}")
    List<AttendancePO> findByCourseIdAndSessionDate(@Param("courseId") Long courseId, @Param("sessionDate") LocalDate sessionDate);

    @Select("SELECT * FROM attendances WHERE course_id = #{courseId} AND student_id = #{studentId} AND session_date = #{sessionDate}")
    Optional<AttendancePO> findByCourseIdAndStudentIdAndSessionDate(@Param("courseId") Long courseId, @Param("studentId") Long studentId, @Param("sessionDate") LocalDate sessionDate);

    @Select("SELECT COUNT(*) FROM attendances WHERE course_id = #{courseId} AND student_id = #{studentId}")
    long countByCourseIdAndStudentId(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

    @Select("SELECT COUNT(*) FROM attendances WHERE course_id = #{courseId} AND student_id = #{studentId} AND status = #{status}")
    long countByCourseIdAndStudentIdAndStatus(@Param("courseId") Long courseId, @Param("studentId") Long studentId, @Param("status") String status);
}
