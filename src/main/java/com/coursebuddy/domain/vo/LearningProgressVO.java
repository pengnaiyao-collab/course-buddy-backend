package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 进度视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgressVO {
    private Long id;
    private Long userId;
    private Long courseId;
    private Long resourceId;
    private Integer progress;
    private Integer studyMinutes;
    private LocalDateTime lastStudiedAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
