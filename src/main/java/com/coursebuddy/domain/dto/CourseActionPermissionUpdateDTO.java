package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseActionPermissionUpdateDTO {

    @NotNull
    private Long courseId;

    @NotBlank
    @Pattern(regexp = "^L[1-4]$", message = "权限级别必须为 L1、L2、L3 或 L4")
    private String permissionLevel;

    @NotBlank
    @Size(max = 32)
    private String actionKey;

    @NotNull
    private Boolean allowed;
}
