package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户资料视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {
    private Long id;
    private String username;
    private String studentNumber;
    private String realName;
    private String school;
    private String avatar;
    private String avatarUrl;
    private String bio;
    private String role;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
