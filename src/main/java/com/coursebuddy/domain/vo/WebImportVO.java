package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebImportVO {
    private Long id;
    private Long courseId;
    private String url;
    private String title;
    private String content;
    private String status;
    private String errorMessage;
    private Long knowledgeItemId;
    private Long createdBy;
    private LocalDateTime createdAt;
}
