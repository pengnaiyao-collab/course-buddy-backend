package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @Size(max = 16, message = "Role must not exceed 16 characters")
    private String role;
}
