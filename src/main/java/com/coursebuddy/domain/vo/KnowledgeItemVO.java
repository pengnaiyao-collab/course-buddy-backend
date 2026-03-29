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
public class KnowledgeItemVO {

    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private String content;
    private String fileUrl;
    private String fileType;
    private String category;
    private String tags;
    private String extractedText;
    private String sourceType;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
