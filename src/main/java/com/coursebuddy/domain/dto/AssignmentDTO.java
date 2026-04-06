package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 作业传输对象
 */
@Data
public class AssignmentDTO {
    private Long courseId;

    @NotBlank
    private String title;

    private String description;
    private LocalDateTime dueDate;
    private Double maxScore;
    private String attachmentUrl;
}
