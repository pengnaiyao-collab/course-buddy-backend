package com.coursebuddy.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method for audit logging. The aspect will record entity type, action,
 * and operator information to the audit_logs table.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    /** Type of entity being operated on, e.g. "KnowledgeItem" */
    String entityType() default "";
    /** Description of the action, e.g. "CREATE", "UPDATE", "DELETE" */
    String action();
}
