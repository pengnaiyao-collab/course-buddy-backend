package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 课程统计视图对象
 */
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
    private Long totalDiscussions;
    private Double averageProgress;
    private Double averageGrade;
    private Double passRate;
    private java.util.Map<String, Long> gradeDistribution; // 例如："90-100": 10
    private java.util.List<java.util.Map<String, Object>> dailyActiveUsers; // 例如：[{date: "2024-03-01", count: 20}]
}
