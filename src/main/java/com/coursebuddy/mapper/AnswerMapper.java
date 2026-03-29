package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.AnswerPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AnswerMapper extends BaseMapper<AnswerPO> {

    @Select("SELECT * FROM answers WHERE question_id = #{questionId}")
    IPage<AnswerPO> findByQuestionId(Page<AnswerPO> page, @Param("questionId") Long questionId);
}
