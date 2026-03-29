package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.CoursePermissionPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CoursePermissionMapper extends BaseMapper<CoursePermissionPO> {

    @Select("SELECT * FROM course_permissions WHERE user_id = #{userId} AND course_id = #{courseId}")
    Optional<CoursePermissionPO> findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Select("SELECT * FROM course_permissions WHERE course_id = #{courseId}")
    List<CoursePermissionPO> findByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT * FROM course_permissions WHERE user_id = #{userId}")
    List<CoursePermissionPO> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM course_permissions WHERE course_id = #{courseId} AND permission_level = #{permissionLevel}")
    List<CoursePermissionPO> findByCourseIdAndPermissionLevel(@Param("courseId") Long courseId, @Param("permissionLevel") String permissionLevel);

    @Select("SELECT COUNT(*) > 0 FROM course_permissions WHERE user_id = #{userId} AND course_id = #{courseId}")
    boolean existsByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Delete("DELETE FROM course_permissions WHERE user_id = #{userId} AND course_id = #{courseId}")
    void deleteByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
}
