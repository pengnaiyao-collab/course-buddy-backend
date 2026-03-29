package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.LearningTaskPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LearningTaskMapper extends BaseMapper<LearningTaskPO> {

    @Select("SELECT * FROM learning_tasks WHERE user_id = #{userId}")
    IPage<LearningTaskPO> findByUserId(Page<LearningTaskPO> page, @Param("userId") Long userId);

    @Select("SELECT * FROM learning_tasks WHERE user_id = #{userId} AND status = #{status}")
    IPage<LearningTaskPO> findByUserIdAndStatus(Page<LearningTaskPO> page, @Param("userId") Long userId, @Param("status") String status);
}
