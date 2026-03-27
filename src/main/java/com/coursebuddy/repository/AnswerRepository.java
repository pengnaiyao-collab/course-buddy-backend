package com.coursebuddy.repository;

import com.coursebuddy.domain.po.AnswerPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<AnswerPO, Long> {

    Page<AnswerPO> findByQuestionId(Long questionId, Pageable pageable);
}
