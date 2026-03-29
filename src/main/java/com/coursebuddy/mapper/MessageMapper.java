package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.MessagePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MessageMapper extends BaseMapper<MessagePO> {

    @Select("SELECT * FROM messages WHERE (sender_id = #{userId1} AND receiver_id = #{userId2}) OR (sender_id = #{userId2} AND receiver_id = #{userId1}) ORDER BY created_at DESC")
    IPage<MessagePO> findConversation(Page<MessagePO> page, @Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Select("SELECT * FROM messages WHERE receiver_id = #{receiverId} ORDER BY created_at DESC")
    IPage<MessagePO> findByReceiverIdOrderByCreatedAtDesc(Page<MessagePO> page, @Param("receiverId") Long receiverId);

    @Select("SELECT COUNT(*) FROM messages WHERE receiver_id = #{receiverId} AND is_read = #{isRead}")
    long countByReceiverIdAndIsRead(@Param("receiverId") Long receiverId, @Param("isRead") Boolean isRead);

    @Update("UPDATE messages SET is_read = true WHERE sender_id = #{senderId} AND receiver_id = #{receiverId} AND is_read = false")
    int markConversationRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}
