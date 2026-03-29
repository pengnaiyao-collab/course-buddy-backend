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
public class KnowledgeResourceVO {
    private Long id;
    private Long knowledgeItemId;
    private String resourceType;
    private String title;
    private String url;
    private String description;
    private Long createdBy;
    private LocalDateTime createdAt;
}
