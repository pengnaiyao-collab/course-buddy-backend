package com.coursebuddy.repository;

import com.coursebuddy.domain.po.CollaborationLogPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollaborationLogRepository extends JpaRepository<CollaborationLogPO, Long> {

    Page<CollaborationLogPO> findByProjectId(Long projectId, Pageable pageable);

    List<CollaborationLogPO> findByProjectIdAndUserId(Long projectId, Long userId);
}
