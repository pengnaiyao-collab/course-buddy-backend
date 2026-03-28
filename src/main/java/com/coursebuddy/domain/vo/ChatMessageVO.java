package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageVO {

    private Long id;
    private Long conversationId;
    private String role;
    private String content;
    private Integer tokenCount;
    private LocalDateTime createdAt;
}
