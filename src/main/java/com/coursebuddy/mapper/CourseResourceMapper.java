package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.CourseResourcePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 课程资源映射器
 */
@Mapper
public interface CourseResourceMapper extends BaseMapper<CourseResourcePO> {

    @Select("SELECT * FROM course_resources WHERE course_id = #{courseId}")
    IPage<CourseResourcePO> findByCourseId(Page<CourseResourcePO> page, @Param("courseId") Long courseId);

    @Update("UPDATE course_resources SET download_count = download_count + 1 WHERE id = #{id}")
    void incrementDownloadCount(@Param("id") Long id);
}
