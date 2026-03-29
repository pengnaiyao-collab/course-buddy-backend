package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AssignmentDTO {
    private Long courseId;

    @NotBlank
    private String title;

    private String description;
    private LocalDateTime dueDate;
    private Integer maxScore;
    private String attachmentUrl;
}
