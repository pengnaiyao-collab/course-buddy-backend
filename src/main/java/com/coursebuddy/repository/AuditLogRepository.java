package com.coursebuddy.repository;

import com.coursebuddy.domain.po.AuditLogPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogPO, Long> {
    Page<AuditLogPO> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);
    Page<AuditLogPO> findByOperatorId(Long operatorId, Pageable pageable);
}
