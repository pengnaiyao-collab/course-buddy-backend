package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.CourseEnrollmentPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface CourseEnrollmentMapper extends BaseMapper<CourseEnrollmentPO> {

    @Select("SELECT * FROM course_enrollments WHERE course_id = #{courseId} AND user_id = #{userId}")
    Optional<CourseEnrollmentPO> findByCourseIdAndUserId(@Param("courseId") Long courseId, @Param("userId") Long userId);

    @Select("SELECT * FROM course_enrollments WHERE user_id = #{userId}")
    IPage<CourseEnrollmentPO> findByUserId(Page<CourseEnrollmentPO> page, @Param("userId") Long userId);

    @Select("SELECT * FROM course_enrollments WHERE user_id = #{userId} AND status = #{status}")
    IPage<CourseEnrollmentPO> findByUserIdAndStatus(Page<CourseEnrollmentPO> page, @Param("userId") Long userId, @Param("status") String status);

    @Select("SELECT * FROM course_enrollments WHERE course_id = #{courseId}")
    IPage<CourseEnrollmentPO> findByCourseId(Page<CourseEnrollmentPO> page, @Param("courseId") Long courseId);

    @Select("SELECT * FROM course_enrollments WHERE course_id = #{courseId} AND status = #{status}")
    IPage<CourseEnrollmentPO> findByCourseIdAndStatus(Page<CourseEnrollmentPO> page, @Param("courseId") Long courseId, @Param("status") String status);

    @Select("SELECT COUNT(*) > 0 FROM course_enrollments WHERE course_id = #{courseId} AND user_id = #{userId}")
    boolean existsByCourseIdAndUserId(@Param("courseId") Long courseId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM course_enrollments WHERE course_id = #{courseId}")
    long countByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT COUNT(*) FROM course_enrollments WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);
}
