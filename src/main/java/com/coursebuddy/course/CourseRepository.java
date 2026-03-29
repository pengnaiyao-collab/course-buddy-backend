package com.coursebuddy.course;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CourseRepository extends BaseMapper<Course> {

    @Select("SELECT * FROM courses WHERE status = #{status}")
    IPage<Course> findByStatus(Page<Course> page, @Param("status") String status);

    @Select("SELECT * FROM courses WHERE teacher_id = #{teacherId}")
    IPage<Course> findByTeacherId(Page<Course> page, @Param("teacherId") Long teacherId);

    @Select("SELECT * FROM courses WHERE status = 'PUBLISHED' AND (LOWER(title) LIKE CONCAT('%', LOWER(#{keyword}), '%') OR LOWER(category) LIKE CONCAT('%', LOWER(#{keyword}), '%'))")
    IPage<Course> searchPublished(Page<Course> page, @Param("keyword") String keyword);
}
