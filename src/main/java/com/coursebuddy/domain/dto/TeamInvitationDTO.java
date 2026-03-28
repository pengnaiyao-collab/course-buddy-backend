package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.Email;
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
public class TeamInvitationDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @Size(max = 16, message = "Role must not exceed 16 characters")
    private String role;
}
