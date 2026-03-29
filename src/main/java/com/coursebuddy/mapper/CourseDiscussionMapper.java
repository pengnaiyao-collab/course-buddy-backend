package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.CourseDiscussionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CourseDiscussionMapper extends BaseMapper<CourseDiscussionPO> {

    @Select("SELECT * FROM course_discussions WHERE course_id = #{courseId} AND parent_id IS NULL AND is_deleted = false")
    IPage<CourseDiscussionPO> findByCourseIdAndParentIdIsNullAndIsDeletedFalse(Page<CourseDiscussionPO> page, @Param("courseId") Long courseId);

    @Select("SELECT * FROM course_discussions WHERE parent_id = #{parentId} AND is_deleted = false ORDER BY created_at ASC")
    List<CourseDiscussionPO> findByParentIdAndIsDeletedFalseOrderByCreatedAtAsc(@Param("parentId") Long parentId);

    @Select("SELECT COUNT(*) FROM course_discussions WHERE course_id = #{courseId} AND parent_id IS NULL AND is_deleted = false")
    long countByCourseIdAndParentIdIsNullAndIsDeletedFalse(@Param("courseId") Long courseId);

    @Update("UPDATE course_discussions SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id);
}
