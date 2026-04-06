package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 课程视图对象
 */
@Data
@Builder
public class CourseVO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long instructorId;
    private String instructorName;
    private Long departmentId;
    private Integer creditHours;
    private String level;
    private Integer capacity;
    private Integer enrolledCount;
    private String thumbnailUrl;
    private String syllabus;
    private Integer maxGrade;
    private Integer passingGrade;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean enrolled;
}
