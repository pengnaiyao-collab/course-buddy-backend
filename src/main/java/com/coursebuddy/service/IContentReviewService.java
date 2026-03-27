package com.coursebuddy.service;

import com.coursebuddy.domain.po.ContentReviewPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IContentReviewService {

    ContentReviewPO submitForReview(String contentType, Long contentId,
                                    Long reviewerId, String comments);

    ContentReviewPO approveReview(Long reviewId);

    ContentReviewPO rejectReview(Long reviewId, String comments);

    Page<ContentReviewPO> listPendingReviews(Pageable pageable);

    ContentReviewPO getReview(Long reviewId);
}
