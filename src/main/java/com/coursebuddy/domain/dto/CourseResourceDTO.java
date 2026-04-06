package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 课程资源传输对象
 */
@Data
public class CourseResourceDTO {
    @NotBlank
    private String title;

    private String description;
    private String resourceType;

    @NotBlank
    private String resourceUrl;

    private Long fileSize;
}
