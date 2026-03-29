package com.coursebuddy.repository;

import com.coursebuddy.domain.po.TaskCommentPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskCommentPO, Long> {

    Page<TaskCommentPO> findByTaskIdAndDeletedAtIsNull(Long taskId, Pageable pageable);

    long countByTaskIdAndDeletedAtIsNull(Long taskId);
}
