package com.coursebuddy.repository;

import com.coursebuddy.domain.po.ContentReviewPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentReviewRepository extends JpaRepository<ContentReviewPO, Long> {

    Page<ContentReviewPO> findByStatus(String status, Pageable pageable);

    List<ContentReviewPO> findByContentTypeAndContentId(String contentType, Long contentId);

    List<ContentReviewPO> findByReviewerId(Long reviewerId);
}
