package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.QuestionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface QuestionMapper extends BaseMapper<QuestionPO> {

    @Select("SELECT * FROM questions WHERE user_id = #{userId}")
    IPage<QuestionPO> findByUserId(Page<QuestionPO> page, @Param("userId") Long userId);

    @Select("SELECT * FROM questions WHERE course_id = #{courseId}")
    IPage<QuestionPO> findByCourseId(Page<QuestionPO> page, @Param("courseId") Long courseId);
}
