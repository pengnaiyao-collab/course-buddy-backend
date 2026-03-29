package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.ConversationMessagePO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConversationMessageMapper extends BaseMapper<ConversationMessagePO> {

    @Select("SELECT * FROM ai_conversation_messages WHERE conversation_id = #{conversationId} ORDER BY created_at ASC")
    List<ConversationMessagePO> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") Long conversationId);

    @Delete("DELETE FROM ai_conversation_messages WHERE conversation_id = #{conversationId}")
    void deleteByConversationId(@Param("conversationId") Long conversationId);
}
