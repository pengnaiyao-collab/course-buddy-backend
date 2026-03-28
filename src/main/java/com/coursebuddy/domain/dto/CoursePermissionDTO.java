package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 课程权限操作 DTO
 */
@Data
public class CoursePermissionDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /**
     * 权限级别: L1（管理员）, L2（核心协作）, L3（选课成员）, L4（访客）
     */
    @NotBlank(message = "权限级别不能为空")
    @Pattern(regexp = "^L[1-4]$", message = "权限级别必须为 L1、L2、L3 或 L4")
    private String permissionLevel;
}
