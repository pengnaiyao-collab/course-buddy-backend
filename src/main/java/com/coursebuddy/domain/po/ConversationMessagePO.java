package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话消息持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_conversation_messages")
public class ConversationMessagePO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long conversationId;
    private String role;
    private String content;
    private String imageData;
    private String imageMimeType;
    private String imageName;
    private Integer tokenCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
