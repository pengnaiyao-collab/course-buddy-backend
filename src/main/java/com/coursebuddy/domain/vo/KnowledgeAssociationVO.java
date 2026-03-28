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
public class KnowledgeAssociationVO {
    private Long id;
    private Long sourceId;
    private Long targetId;
    private String relationType;
    private String description;
    private Long createdBy;
    private LocalDateTime createdAt;
    /** Target knowledge item title for display */
    private String targetTitle;
}
