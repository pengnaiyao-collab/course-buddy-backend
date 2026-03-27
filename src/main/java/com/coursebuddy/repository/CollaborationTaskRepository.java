package com.coursebuddy.repository;

import com.coursebuddy.domain.po.CollaborationTaskPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollaborationTaskRepository extends JpaRepository<CollaborationTaskPO, Long> {

    Page<CollaborationTaskPO> findByProjectId(Long projectId, Pageable pageable);

    Page<CollaborationTaskPO> findByAssigneeId(Long assigneeId, Pageable pageable);
}
