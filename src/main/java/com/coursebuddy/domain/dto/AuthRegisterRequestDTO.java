package com.coursebuddy.domain.dto;

import com.coursebuddy.domain.auth.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRegisterRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 64, message = "Username must be between 3 and 64 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 128, message = "Password must be between 6 and 128 characters")
    private String password;

    private String fullName;

    private Role role;
}
