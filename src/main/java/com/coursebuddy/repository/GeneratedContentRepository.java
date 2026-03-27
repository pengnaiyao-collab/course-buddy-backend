package com.coursebuddy.repository;

import com.coursebuddy.domain.po.GeneratedContentPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneratedContentRepository extends JpaRepository<GeneratedContentPO, Long> {

    Page<GeneratedContentPO> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<GeneratedContentPO> findByUserIdAndContentTypeOrderByCreatedAtDesc(
            Long userId, String contentType, Pageable pageable);
}
