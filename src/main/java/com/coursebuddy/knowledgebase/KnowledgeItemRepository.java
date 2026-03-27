package com.coursebuddy.knowledgebase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeItemRepository extends JpaRepository<KnowledgeItem, Long> {

    Page<KnowledgeItem> findByCourseId(Long courseId, Pageable pageable);

    Page<KnowledgeItem> findByCourseIdAndTitleContainingIgnoreCase(Long courseId, String keyword, Pageable pageable);

    Page<KnowledgeItem> findByCourseIdAndCategory(Long courseId, String category, Pageable pageable);
}
