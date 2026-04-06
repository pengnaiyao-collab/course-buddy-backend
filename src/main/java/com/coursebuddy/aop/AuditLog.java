package com.coursebuddy.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记方法用于审计日志记录，切面会将实体类型、动作和操作者信息写入 audit_logs 表。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    /** 操作的实体类型，例如 "KnowledgeItem" */
    String entityType() default "";
    /** 动作描述，例如 "CREATE"、"UPDATE"、"DELETE" */
    String action();
}
