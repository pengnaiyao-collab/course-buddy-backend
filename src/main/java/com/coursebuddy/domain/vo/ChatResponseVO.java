package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天响应视图对象
 */
@Data
@Builder
public class ChatResponseVO {

    private Long conversationId;
    private String title;
    private String answer;
    private List<ChatMessageVO> messages;
    private List<KnowledgeSourceVO> sources;
    private List<Long> relatedKnowledgeIds;
    private LocalDateTime createdAt;
}
