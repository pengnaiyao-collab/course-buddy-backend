package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 进度传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgressDTO {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    private Long resourceId;

    @Min(value = 0, message = "Progress must be between 0 and 100")
    @Max(value = 100, message = "Progress must be between 0 and 100")
    private Integer progress;

    @Min(value = 0, message = "Study minutes must be non-negative")
    private Integer studyMinutes;

    private String notes;
}
