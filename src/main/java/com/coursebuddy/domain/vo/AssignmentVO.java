package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 作业视图对象
 */
@Data
@Builder
public class AssignmentVO {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Double maxScore;
    private String attachmentUrl;
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
