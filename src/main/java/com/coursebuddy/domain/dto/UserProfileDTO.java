package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    @Size(max = 50, message = "Real name must not exceed 50 characters")
    private String realName;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    private String bio;
}
