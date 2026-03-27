package com.coursebuddy.domain.vo;

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
public class LearningTaskVO {

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
}
