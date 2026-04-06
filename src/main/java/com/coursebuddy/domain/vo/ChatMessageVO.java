package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息视图对象
 */
@Data
@Builder
public class ChatMessageVO {

    private Long id;
    private Long conversationId;
    private String role;
    private String content;
    private String imageData;
    private String imageMimeType;
    private String imageName;
    private Integer tokenCount;
    private LocalDateTime createdAt;
}
