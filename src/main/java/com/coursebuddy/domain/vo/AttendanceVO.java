package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤视图对象
 */
@Data
@Builder
public class AttendanceVO {
    private Long id;
    private Long courseId;
    private Long studentId;
    private LocalDate sessionDate;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
