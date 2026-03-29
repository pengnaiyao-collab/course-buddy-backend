package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

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
