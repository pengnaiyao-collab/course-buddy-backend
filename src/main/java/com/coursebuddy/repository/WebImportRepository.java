package com.coursebuddy.repository;

import com.coursebuddy.domain.po.WebImportPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebImportRepository extends JpaRepository<WebImportPO, Long> {
    Page<WebImportPO> findByCourseId(Long courseId, Pageable pageable);
    Page<WebImportPO> findByCourseIdAndStatus(Long courseId, String status, Pageable pageable);
}
