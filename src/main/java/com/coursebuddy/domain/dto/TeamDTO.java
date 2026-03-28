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
public class TeamDTO {

    @NotBlank(message = "Team name is required")
    @Size(max = 128, message = "Team name must not exceed 128 characters")
    private String name;

    private String description;

    private String avatarUrl;

    private Long courseId;

    private Long projectId;
}
