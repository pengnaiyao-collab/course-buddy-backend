package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AiUsageStatsVO {

    private Long id;
    private Long userId;
    private String model;
    private String requestType;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Long durationMs;
    private String status;
    private LocalDateTime createdAt;
}
