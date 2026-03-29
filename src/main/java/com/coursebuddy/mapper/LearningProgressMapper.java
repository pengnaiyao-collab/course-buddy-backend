package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.LearningProgressPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface LearningProgressMapper extends BaseMapper<LearningProgressPO> {

    @Select("SELECT * FROM learning_progress WHERE user_id = #{userId} AND course_id = #{courseId}")
    Optional<LearningProgressPO> findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Select("SELECT * FROM learning_progress WHERE user_id = #{userId}")
    IPage<LearningProgressPO> findByUserId(Page<LearningProgressPO> page, @Param("userId") Long userId);

    @Select("SELECT * FROM learning_progress WHERE course_id = #{courseId}")
    List<LearningProgressPO> findByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT AVG(progress) FROM learning_progress WHERE course_id = #{courseId}")
    Double getAverageProgressByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT SUM(study_minutes) FROM learning_progress WHERE user_id = #{userId}")
    Long getTotalStudyMinutesByUserId(@Param("userId") Long userId);
}
