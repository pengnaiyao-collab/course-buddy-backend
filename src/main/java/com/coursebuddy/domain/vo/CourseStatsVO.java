package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseStatsVO {
    private Long courseId;
    private Long totalEnrollments;
    private Long activeEnrollments;
    private Long completedEnrollments;
    private Long totalLessons;
    private Long publishedLessons;
    private Long totalAssignments;
    private Double averageProgress;
}
