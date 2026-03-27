package com.coursebuddy.repository;

import com.coursebuddy.domain.po.KnowledgeItemPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeItemRepository extends JpaRepository<KnowledgeItemPO, Long> {

    Page<KnowledgeItemPO> findByCourseId(Long courseId, Pageable pageable);

    Page<KnowledgeItemPO> findByCourseIdAndTitleContainingIgnoreCase(Long courseId, String keyword, Pageable pageable);

    Page<KnowledgeItemPO> findByCourseIdAndCategory(Long courseId, String category, Pageable pageable);
}
