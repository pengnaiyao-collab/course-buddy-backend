package com.coursebuddy.aiassistant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningTaskResponse {

    private Long id;
    private Long userId;
    private Long courseId;
    private String title;
    private String description;
    private String status;
    private LocalDate dueDate;
    private String priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LearningTaskResponse from(LearningTask task) {
        return LearningTaskResponse.builder()
                .id(task.getId())
                .userId(task.getUserId())
                .courseId(task.getCourseId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
