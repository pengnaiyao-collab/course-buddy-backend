package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.ConversationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 会话映射器
 */
@Mapper
public interface ConversationMapper extends BaseMapper<ConversationPO> {

    @Select("SELECT * FROM ai_conversations WHERE user_id = #{userId} ORDER BY updated_at DESC")
    IPage<ConversationPO> findByUserIdOrderByUpdatedAtDesc(Page<ConversationPO> page, @Param("userId") Long userId);

    @Select("SELECT * FROM ai_conversations WHERE user_id = #{userId} AND status = #{status} ORDER BY updated_at DESC")
    IPage<ConversationPO> findByUserIdAndStatusOrderByUpdatedAtDesc(Page<ConversationPO> page, @Param("userId") Long userId, @Param("status") String status);
}
