package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CourseDTO {
    private String code;

    @NotBlank
    private String name;

    private String description;
    private Integer creditHours;
    private String level;
    private Integer capacity;
    private String thumbnailUrl;
    private String syllabus;
    private Integer maxGrade;
    private Integer passingGrade;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long departmentId;
}
