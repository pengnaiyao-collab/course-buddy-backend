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
public class OcrResultVO {
    private Long id;
    private Long fileUploadId;
    private String objectName;
    private String extractedText;
    private String structuredSummary;
    private Long knowledgeItemId;
    private Double confidence;
    private String language;
    private String status;
    private String errorMessage;
    private Long createdBy;
    private LocalDateTime createdAt;
    private boolean ocrAvailable;
}
