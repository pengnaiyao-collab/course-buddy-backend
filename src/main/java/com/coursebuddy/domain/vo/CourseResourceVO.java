package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CourseResourceVO {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private String resourceType;
    private String resourceUrl;
    private Long fileSize;
    private Integer downloadCount;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
