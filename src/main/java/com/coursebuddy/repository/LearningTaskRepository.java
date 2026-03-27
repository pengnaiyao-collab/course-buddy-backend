package com.coursebuddy.repository;

import com.coursebuddy.domain.po.LearningTaskPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearningTaskRepository extends JpaRepository<LearningTaskPO, Long> {

    Page<LearningTaskPO> findByUserId(Long userId, Pageable pageable);

    Page<LearningTaskPO> findByUserIdAndStatus(Long userId, String status, Pageable pageable);
}
