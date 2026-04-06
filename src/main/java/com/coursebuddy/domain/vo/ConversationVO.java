package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话视图对象
 */
@Data
@Builder
public class ConversationVO {

    private Long id;
    private Long userId;
    private String title;
    private String model;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
