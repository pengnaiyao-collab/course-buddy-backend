package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GeneratedContentVO {

    private Long id;
    private Long userId;
    private String contentType;
    private String subject;
    private String prompt;
    private String content;
    private Long courseId;
    private String status;
    private Integer tokenCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
