package com.coursebuddy.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.po.ContentReviewDecisionPO;
import com.coursebuddy.domain.po.ContentReviewPO;
import com.coursebuddy.domain.po.FileUploadPO;
import com.coursebuddy.mapper.ContentReviewDecisionMapper;
import com.coursebuddy.mapper.ContentReviewMapper;
import com.coursebuddy.mapper.FileUploadMapper;
import com.coursebuddy.service.IContentReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 内容审核服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ContentReviewServiceImpl implements IContentReviewService {

    private final ContentReviewMapper reviewRepository;
    private final ContentReviewDecisionMapper decisionRepository;
    private final FileUploadMapper fileUploadRepository;

    @Override
    @Transactional
    public ContentReviewPO submitForReview(String contentType, Long contentId, Long reviewerId,
                                           Integer requiredApprovals, String comments) {
        ContentReviewPO review = ContentReviewPO.builder()
                .contentType(contentType)
                .contentId(contentId)
                .reviewerId(reviewerId)
                .requiredApprovals(requiredApprovals == null ? 2 : Math.max(1, requiredApprovals))
                .approvalCount(0)
                .status("PENDING")
                .moderationStatus("NORMAL")
                .comments(comments)
                .build();
        reviewRepository.insert(review);
        ContentReviewPO saved = review;
        log.info("Submitted for review: {}/{} requiredApprovals={}",
                contentType, contentId, saved.getRequiredApprovals());
        return saved;
    }

    @Override
    @Transactional
    public ContentReviewPO approveReview(Long reviewId, Long reviewerId, String comments) {
        ContentReviewPO review = findAndCheckActive(reviewId);
        ensureNoDuplicateDecision(reviewId, reviewerId);

        decisionRepository.insert(ContentReviewDecisionPO.builder()
                .reviewId(reviewId)
                .reviewerId(reviewerId)
                .decision("APPROVE")
                .comments(comments)
                .build());

        long approvals = decisionRepository.countByReviewIdAndDecision(reviewId, "APPROVE");
        review.setApprovalCount((int) approvals);
        // 注意：如需多个审核人，建议使用独立的审核人列表表
        // 当前 reviewerId 与 secondReviewerId 的实现仅支持 2 名审核人
        if (review.getReviewerId() == null) {
            review.setReviewerId(reviewerId);
        } else if (!review.getReviewerId().equals(reviewerId) && review.getSecondReviewerId() == null) {
            review.setSecondReviewerId(reviewerId);
        }

        if (approvals >= review.getRequiredApprovals()) {
            review.setStatus("APPROVED");
            review.setReviewedAt(LocalDateTime.now());
        } else {
            review.setStatus("IN_REVIEW");
        }
        reviewRepository.updateById(review);
        return review;
    }

    @Override
    @Transactional
    public ContentReviewPO rejectReview(Long reviewId, Long reviewerId, String comments) {
        ContentReviewPO review = findAndCheckActive(reviewId);
        ensureNoDuplicateDecision(reviewId, reviewerId);

        decisionRepository.insert(ContentReviewDecisionPO.builder()
                .reviewId(reviewId)
                .reviewerId(reviewerId)
                .decision("REJECT")
                .comments(comments)
                .build());

        review.setStatus("REJECTED");
        review.setComments(comments);
        review.setReviewedAt(LocalDateTime.now());
        reviewRepository.updateById(review);
        return review;
    }

    @Override
    @Transactional
    public ContentReviewPO markViolationAndTakedown(Long reviewId, Long reviewerId, String reason) {
        ContentReviewPO review = reviewRepository.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "Review not found: " + reviewId);
        }
        decisionRepository.insert(ContentReviewDecisionPO.builder()
                .reviewId(reviewId)
                .reviewerId(reviewerId)
                .decision("TAKEDOWN")
                .comments(reason)
                .build());

        review.setModerationStatus("TAKEDOWN");
        review.setViolationReason(reason);
        review.setStatus("TAKEDOWNED");
        review.setReviewedAt(LocalDateTime.now());
        applyModeration(review, false);
        reviewRepository.updateById(review);
        return review;
    }

    @Override
    @Transactional
    public ContentReviewPO markViolationAndRemove(Long reviewId, Long reviewerId, String reason) {
        ContentReviewPO review = reviewRepository.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "Review not found: " + reviewId);
        }
        decisionRepository.insert(ContentReviewDecisionPO.builder()
                .reviewId(reviewId)
                .reviewerId(reviewerId)
                .decision("REMOVE")
                .comments(reason)
                .build());

        review.setModerationStatus("REMOVED");
        review.setViolationReason(reason);
        review.setStatus("REMOVED");
        review.setReviewedAt(LocalDateTime.now());
        applyModeration(review, true);
        reviewRepository.updateById(review);
        return review;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContentReviewPO> listPendingReviews(Pageable pageable) {
        IPage<ContentReviewPO> poPage = reviewRepository.findByStatusIn(
                MybatisPlusPageUtils.toMpPage(pageable), Set.of("PENDING", "IN_REVIEW"));
        return MybatisPlusPageUtils.toSpringPage(poPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ContentReviewPO getReview(Long reviewId) {
        ContentReviewPO review = reviewRepository.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "Review not found: " + reviewId);
        }
        return review;
    }

    private void applyModeration(ContentReviewPO review, boolean hardRemove) {
        String type = review.getContentType() == null ? "" : review.getContentType().toUpperCase();
        if ("FILE_UPLOAD".equals(type) || "FILE".equals(type)) {
            FileUploadPO file = fileUploadRepository.selectById(review.getContentId());
            if (file == null) {
                throw new BusinessException(404, "File not found");
            }
            file.setIsDeleted(true);
            fileUploadRepository.updateById(file);
        }
    }

    private void ensureNoDuplicateDecision(Long reviewId, Long reviewerId) {
        if (decisionRepository.findByReviewIdAndReviewerId(reviewId, reviewerId).isPresent()) {
            throw new BusinessException(409, "该审核人已处理过此记录");
        }
    }

    private ContentReviewPO findAndCheckActive(Long reviewId) {
        ContentReviewPO review = reviewRepository.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "Review not found: " + reviewId);
        }
        if (!"PENDING".equals(review.getStatus()) && !"IN_REVIEW".equals(review.getStatus())) {
            throw new BusinessException("Review is no longer active, current status: " + review.getStatus());
        }
        return review;
    }
}
