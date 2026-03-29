package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.GeneratedContentPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GeneratedContentMapper extends BaseMapper<GeneratedContentPO> {

    @Select("SELECT * FROM ai_generated_contents WHERE user_id = #{userId} ORDER BY created_at DESC")
    IPage<GeneratedContentPO> findByUserIdOrderByCreatedAtDesc(Page<GeneratedContentPO> page, @Param("userId") Long userId);

    @Select("SELECT * FROM ai_generated_contents WHERE user_id = #{userId} AND content_type = #{contentType} ORDER BY created_at DESC")
    IPage<GeneratedContentPO> findByUserIdAndContentTypeOrderByCreatedAtDesc(
            Page<GeneratedContentPO> page, @Param("userId") Long userId, @Param("contentType") String contentType);
}
