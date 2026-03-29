package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.AiUsageStatsPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiUsageStatsMapper extends BaseMapper<AiUsageStatsPO> {

    @Select("SELECT * FROM ai_usage_stats WHERE user_id = #{userId} ORDER BY created_at DESC")
    IPage<AiUsageStatsPO> findByUserIdOrderByCreatedAtDesc(Page<AiUsageStatsPO> page, @Param("userId") Long userId);

    @Select("SELECT COALESCE(SUM(total_tokens), 0) FROM ai_usage_stats WHERE user_id = #{userId}")
    Long sumTotalTokensByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM ai_usage_stats WHERE user_id = #{userId} AND status = 'SUCCESS'")
    long countSuccessByUserId(@Param("userId") Long userId);
}
