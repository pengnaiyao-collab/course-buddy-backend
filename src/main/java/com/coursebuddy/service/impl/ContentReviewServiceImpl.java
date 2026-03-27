package com.coursebuddy.service.impl;

import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.po.ContentReviewPO;
import com.coursebuddy.repository.ContentReviewRepository;
import com.coursebuddy.service.IContentReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContentReviewServiceImpl implements IContentReviewService {

    private final ContentReviewRepository reviewRepository;

    @Override
    @Transactional
    public ContentReviewPO submitForReview(String contentType, Long contentId,
                                           Long reviewerId, String comments) {
        ContentReviewPO review = ContentReviewPO.builder()
                .contentType(contentType)
                .contentId(contentId)
                .reviewerId(reviewerId)
                .status("PENDING")
                .comments(comments)
                .build();
        ContentReviewPO saved = reviewRepository.save(review);
        log.info("Submitted for review: {}/{} by reviewer {}", contentType, contentId, reviewerId);
        return saved;
    }

    @Override
    @Transactional
    public ContentReviewPO approveReview(Long reviewId) {
        ContentReviewPO review = findAndCheckPending(reviewId);
        review.setStatus("APPROVED");
        review.setReviewedAt(LocalDateTime.now());
        ContentReviewPO saved = reviewRepository.save(review);
        log.info("Review {} approved", reviewId);
        return saved;
    }

    @Override
    @Transactional
    public ContentReviewPO rejectReview(Long reviewId, String comments) {
        ContentReviewPO review = findAndCheckPending(reviewId);
        review.setStatus("REJECTED");
        review.setComments(comments);
        review.setReviewedAt(LocalDateTime.now());
        ContentReviewPO saved = reviewRepository.save(review);
        log.info("Review {} rejected", reviewId);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContentReviewPO> listPendingReviews(Pageable pageable) {
        return reviewRepository.findByStatus("PENDING", pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ContentReviewPO getReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(404, "Review not found: " + reviewId));
    }

    private ContentReviewPO findAndCheckPending(Long reviewId) {
        ContentReviewPO review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(404, "Review not found: " + reviewId));
        if (!"PENDING".equals(review.getStatus())) {
            throw new BusinessException("Review is no longer pending, current status: "
                    + review.getStatus());
        }
        return review;
    }
}
