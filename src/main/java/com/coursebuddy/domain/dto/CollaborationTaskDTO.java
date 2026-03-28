package com.coursebuddy.domain.dto;

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
public class CollaborationTaskDTO {

    @NotBlank(message = "Task title is required")
    @Size(max = 256, message = "Title must not exceed 256 characters")
    private String title;

    private String description;

    private Long projectId;

    private Long assigneeId;

    @Size(max = 16, message = "Status must not exceed 16 characters")
    private String status;

    @Size(max = 16, message = "Priority must not exceed 16 characters")
    private String priority;

    private LocalDate dueDate;
}
