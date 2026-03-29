package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;

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
