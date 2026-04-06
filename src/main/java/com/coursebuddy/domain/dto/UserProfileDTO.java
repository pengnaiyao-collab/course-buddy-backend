package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户资料传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    @Size(max = 50, message = "Real name must not exceed 50 characters")
    private String realName;

    private String school;

    private String bio;
}
