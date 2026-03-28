package com.coursebuddy.domain.po;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLogPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "old_value", columnDefinition = "LONGTEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "LONGTEXT")
    private String newValue;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name", length = 100)
    private String operatorName;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
