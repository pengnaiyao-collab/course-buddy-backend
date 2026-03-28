package com.coursebuddy.repository;

import com.coursebuddy.domain.po.TaskAttachmentPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachmentPO, Long> {

    List<TaskAttachmentPO> findByTaskId(Long taskId);

    long countByTaskId(Long taskId);
}
