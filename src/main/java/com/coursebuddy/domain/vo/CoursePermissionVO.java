package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程权限 VO
 */
@Data
@Builder
public class CoursePermissionVO {
    private Long id;
    private Long userId;
    private Long courseId;
    private String permissionLevel;
    private String permissionLevelName;
    private Long grantedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
