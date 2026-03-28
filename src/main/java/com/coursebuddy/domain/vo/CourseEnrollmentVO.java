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
public class CourseEnrollmentVO {
    private Long id;
    private Long courseId;
    private Long userId;
    private String status;
    private LocalDateTime enrolledAt;
    private LocalDateTime droppedAt;
    private LocalDateTime completedAt;
}
