package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 考勤统计视图对象
 */
@Data
@Builder
public class AttendanceStatsVO {
    private Long courseId;
    private Long studentId;
    private Long totalSessions;
    private Long presentCount;
    private Long absentCount;
    private Long lateCount;
    private Long excusedCount;
    private Double attendanceRate;
}
