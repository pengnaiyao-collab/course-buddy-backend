package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.domain.auth.User;
import com.coursebuddy.domain.po.ContentReviewPO;
import com.coursebuddy.service.IContentReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 内容审核控制器
 */
@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
public class ContentReviewController {

    private final IContentReviewService reviewService;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ContentReviewPO> submitForReview(
            @RequestParam String contentType,
            @RequestParam Long contentId,
            @RequestParam(defaultValue = "2") Integer requiredApprovals,
            @RequestParam(required = false) String comments) {
        User currentUser = SecurityUtils.getCurrentUser();
        return ApiResponse.success(
                reviewService.submitForReview(contentType, contentId, currentUser.getId(), requiredApprovals, comments)
        );
    }

    @PostMapping("/{reviewId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ContentReviewPO> approveReview(
            @PathVariable Long reviewId,
            @RequestParam(required = false) String comments) {
        User currentUser = SecurityUtils.getCurrentUser();
        return ApiResponse.success(reviewService.approveReview(reviewId, currentUser.getId(), comments));
    }

    @PostMapping("/{reviewId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ContentReviewPO> rejectReview(
            @PathVariable Long reviewId,
            @RequestParam(required = false) String comments) {
        User currentUser = SecurityUtils.getCurrentUser();
        return ApiResponse.success(reviewService.rejectReview(reviewId, currentUser.getId(), comments));
    }

    @PostMapping("/{reviewId}/violation/takedown")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ContentReviewPO> takedownByViolation(
            @PathVariable Long reviewId,
            @RequestParam String reason) {
        User currentUser = SecurityUtils.getCurrentUser();
        return ApiResponse.success(reviewService.markViolationAndTakedown(reviewId, currentUser.getId(), reason));
    }

    @PostMapping("/{reviewId}/violation/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ContentReviewPO> removeByViolation(
            @PathVariable Long reviewId,
            @RequestParam String reason) {
        User currentUser = SecurityUtils.getCurrentUser();
        return ApiResponse.success(reviewService.markViolationAndRemove(reviewId, currentUser.getId(), reason));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<ContentReviewPO>> listPendingReviews(Pageable pageable) {
        return ApiResponse.success(reviewService.listPendingReviews(pageable));
    }

    @GetMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ContentReviewPO> getReview(@PathVariable Long reviewId) {
        return ApiResponse.success(reviewService.getReview(reviewId));
    }
}
