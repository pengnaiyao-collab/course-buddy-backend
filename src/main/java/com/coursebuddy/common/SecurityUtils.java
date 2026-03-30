package com.coursebuddy.common;

import com.coursebuddy.auth.Role;
import com.coursebuddy.auth.User;
import com.coursebuddy.common.exception.BusinessException;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        if (principal instanceof com.coursebuddy.domain.auth.User user) {
            return User.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .fullName(user.getFullName())
                    .role(Role.valueOf(user.getRole().name()))
                    .enabled(user.isEnabled())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        }
        throw new BusinessException(401, "用户未登录或认证已过期");
    }
}
