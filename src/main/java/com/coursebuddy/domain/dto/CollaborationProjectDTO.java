package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollaborationProjectDTO {

    @NotBlank(message = "Project name is required")
    @Size(max = 256, message = "Project name must not exceed 256 characters")
    private String name;

    private String description;

    private Long courseId;

    @Size(max = 16, message = "Status must not exceed 16 characters")
    private String status;

    @Size(max = 512, message = "Cover URL must not exceed 512 characters")
    private String coverUrl;

    private Boolean isPublic;
}
