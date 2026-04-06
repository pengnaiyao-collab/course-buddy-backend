package com.coursebuddy.common;

import com.coursebuddy.domain.auth.Role;
import com.coursebuddy.domain.auth.User;
import com.coursebuddy.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 * 提供获取当前登录用户、权限校验等便捷方法
 */
@Slf4j
public class SecurityUtils {

    /**
     * 获取当前登录用户信息
     */
    public static User getCurrentUser() {
        User user = getOptionalUser();
        if (user == null) {
            throw new BusinessException(401, "用户未登录");
        }
        return user;
    }

    /**
     * 获取当前登录用户信息（如果未登录则返回 null）
     */
    public static User getOptionalUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User user) {
            return user;
        }

        log.error("未知的 Principal 类型: {}", principal != null ? principal.getClass().getName() : "null");
        return null;
    }

    /**
     * 判断当前用户是否具有管理员权限
     */
    public static boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    /**
     * 判断当前用户是否具有特定角色
     */
    public static boolean hasRole(Role role) {
        User user = getOptionalUser();
        return user != null && user.getRole() == role;
    }
}
