package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class LessonVO {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private String content;
    private Integer lessonOrder;
    private Integer duration;
    private String videoUrl;
    private String resourceUrls;
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
