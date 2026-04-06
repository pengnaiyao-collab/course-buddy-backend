package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课程选课视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseEnrollmentVO {
    private Long id;
    private Long courseId;
    private Long userId;
    private Long studentId;
    private String studentName;
    private String username;
    private String status;
    private LocalDateTime enrolledAt;
    private LocalDateTime droppedAt;
    private LocalDateTime completedAt;
}
