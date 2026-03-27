package com.coursebuddy.aiassistant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningTaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 256, message = "Title must not exceed 256 characters")
    private String title;

    private String description;

    private Long courseId;

    private String status;

    private LocalDate dueDate;

    @Size(max = 16, message = "Priority must not exceed 16 characters")
    private String priority;
}
