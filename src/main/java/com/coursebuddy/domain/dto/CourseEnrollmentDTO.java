package com.coursebuddy.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程选课传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseEnrollmentDTO {
    private Long courseId;
}
