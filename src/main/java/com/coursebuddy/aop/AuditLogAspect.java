package com.coursebuddy.aop;

import com.coursebuddy.domain.po.AuditLogPO;
import com.coursebuddy.mapper.AuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 审计日志切面
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogMapper auditLogRepository;

    @Around("@annotation(com.coursebuddy.aop.AuditLog)")
    public Object logAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditLog annotation = method.getAnnotation(AuditLog.class);

        String entityType = annotation.entityType();
        String action = annotation.action();
        String operatorName = null;
        Long operatorId = null;
        String ipAddress = null;

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.coursebuddy.domain.auth.User user) {
                operatorId = user.getId();
                operatorName = user.getUsername();
            } else if (auth != null && auth.getPrincipal() instanceof UserDetails ud) {
                operatorName = ud.getUsername();
            }
        } catch (Exception e) {
            log.debug("Failed to extract user info from security context", e);
        }

        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                ipAddress = getClientIp(req);
            }
        } catch (Exception e) {
            log.debug("Failed to extract IP address from request context", e);
        }

        Object result = joinPoint.proceed();

        try {
            auditLogRepository.insert(AuditLogPO.builder()
                    .entityType(entityType)
                    .action(action)
                    .operatorId(operatorId)
                    .operatorName(operatorName)
                    .ipAddress(ipAddress)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to save audit log for action {}/{}: {}", entityType, action, e.getMessage());
        }

        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
