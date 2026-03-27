package com.coursebuddy.aiassistant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearningTaskRepository extends JpaRepository<LearningTask, Long> {

    Page<LearningTask> findByUserId(Long userId, Pageable pageable);

    Page<LearningTask> findByUserIdAndStatus(Long userId, String status, Pageable pageable);
}
