package com.coursebuddy.common;

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
        throw new BusinessException(401, "User not authenticated");
    }
}
