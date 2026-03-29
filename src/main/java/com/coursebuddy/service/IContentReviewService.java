package com.coursebuddy.service;

import com.coursebuddy.domain.po.ContentReviewPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IContentReviewService {

    ContentReviewPO submitForReview(String contentType, Long contentId,
                                    Long reviewerId, Integer requiredApprovals, String comments);

    ContentReviewPO approveReview(Long reviewId, Long reviewerId, String comments);

    ContentReviewPO rejectReview(Long reviewId, Long reviewerId, String comments);

    ContentReviewPO markViolationAndTakedown(Long reviewId, Long reviewerId, String reason);

    ContentReviewPO markViolationAndRemove(Long reviewId, Long reviewerId, String reason);

    Page<ContentReviewPO> listPendingReviews(Pageable pageable);

    ContentReviewPO getReview(Long reviewId);
}
