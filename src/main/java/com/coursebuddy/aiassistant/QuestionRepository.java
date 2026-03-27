package com.coursebuddy.aiassistant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    Page<Question> findByUserId(Long userId, Pageable pageable);

    Page<Question> findByCourseId(Long courseId, Pageable pageable);
}
