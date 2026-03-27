package com.coursebuddy.repository;

import com.coursebuddy.domain.po.QuestionPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionPO, Long> {

    Page<QuestionPO> findByUserId(Long userId, Pageable pageable);

    Page<QuestionPO> findByCourseId(Long courseId, Pageable pageable);
}
