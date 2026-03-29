package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.CourseActionPermissionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CourseActionPermissionMapper extends BaseMapper<CourseActionPermissionPO> {

    @Select("SELECT * FROM course_action_permissions WHERE course_id = #{courseId}")
    List<CourseActionPermissionPO> findByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT * FROM course_action_permissions WHERE course_id = #{courseId} AND permission_level = #{permissionLevel} AND action_key = #{actionKey}")
    Optional<CourseActionPermissionPO> findByCourseIdAndPermissionLevelAndActionKey(
            @Param("courseId") Long courseId, @Param("permissionLevel") String permissionLevel, @Param("actionKey") String actionKey);
}
