package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.po.ContentReviewPO;
import com.coursebuddy.service.IContentReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
public class ContentReviewController {

    private final IContentReviewService reviewService;

    @PostMapping("/submit")
    public ApiResponse<ContentReviewPO> submitForReview(
            @RequestParam String contentType,
            @RequestParam Long contentId,
            @RequestParam Long reviewerId,
            @RequestParam(defaultValue = "2") Integer requiredApprovals,
            @RequestParam(required = false) String comments) {
        return ApiResponse.success(
                reviewService.submitForReview(contentType, contentId, reviewerId, requiredApprovals, comments)
        );
    }

    @PostMapping("/{reviewId}/approve")
    public ApiResponse<ContentReviewPO> approveReview(
            @PathVariable Long reviewId,
            @RequestParam Long reviewerId,
            @RequestParam(required = false) String comments) {
        return ApiResponse.success(reviewService.approveReview(reviewId, reviewerId, comments));
    }

    @PostMapping("/{reviewId}/reject")
    public ApiResponse<ContentReviewPO> rejectReview(
            @PathVariable Long reviewId,
            @RequestParam Long reviewerId,
            @RequestParam(required = false) String comments) {
        return ApiResponse.success(reviewService.rejectReview(reviewId, reviewerId, comments));
    }

    @PostMapping("/{reviewId}/violation/takedown")
    public ApiResponse<ContentReviewPO> takedownByViolation(
            @PathVariable Long reviewId,
            @RequestParam Long reviewerId,
            @RequestParam String reason) {
        return ApiResponse.success(reviewService.markViolationAndTakedown(reviewId, reviewerId, reason));
    }

    @PostMapping("/{reviewId}/violation/remove")
    public ApiResponse<ContentReviewPO> removeByViolation(
            @PathVariable Long reviewId,
            @RequestParam Long reviewerId,
            @RequestParam String reason) {
        return ApiResponse.success(reviewService.markViolationAndRemove(reviewId, reviewerId, reason));
    }

    @GetMapping("/pending")
    public ApiResponse<Page<ContentReviewPO>> listPendingReviews(Pageable pageable) {
        return ApiResponse.success(reviewService.listPendingReviews(pageable));
    }

    @GetMapping("/{reviewId}")
    public ApiResponse<ContentReviewPO> getReview(@PathVariable Long reviewId) {
        return ApiResponse.success(reviewService.getReview(reviewId));
    }
}
