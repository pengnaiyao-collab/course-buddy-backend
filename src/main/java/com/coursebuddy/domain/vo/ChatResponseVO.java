package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
