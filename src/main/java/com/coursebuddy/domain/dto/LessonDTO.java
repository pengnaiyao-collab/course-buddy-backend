package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LessonDTO {
    private Long courseId;

    @NotBlank
    private String title;

    private String description;
    private String content;
    private Integer lessonOrder;
    private Integer duration;
    private String videoUrl;
    private String resourceUrls;
}
