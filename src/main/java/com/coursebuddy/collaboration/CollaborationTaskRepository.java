package com.coursebuddy.collaboration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollaborationTaskRepository extends JpaRepository<CollaborationTask, Long> {

    Page<CollaborationTask> findByProjectId(Long projectId, Pageable pageable);

    Page<CollaborationTask> findByAssigneeId(Long assigneeId, Pageable pageable);
}
