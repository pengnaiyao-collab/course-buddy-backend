package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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
