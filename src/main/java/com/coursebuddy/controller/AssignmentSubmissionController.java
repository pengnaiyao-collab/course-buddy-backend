package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.AssignmentSubmissionDTO;
import com.coursebuddy.domain.dto.GradeSubmissionDTO;
import com.coursebuddy.domain.vo.AssignmentSubmissionCountVO;
import com.coursebuddy.domain.vo.AssignmentSubmissionVO;
import com.coursebuddy.service.IAssignmentSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作业提交控制器
 */
@Tag(name = "Assignment Submissions", description = "Assignment submission management endpoints")
@RestController
@RequestMapping("/v1/submissions")
@RequiredArgsConstructor
public class AssignmentSubmissionController {

    private final IAssignmentSubmissionService submissionService;

    @Operation(summary = "Submit an assignment", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/assignments/{assignmentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AssignmentSubmissionVO> submitAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody AssignmentSubmissionDTO dto) {
        return ApiResponse.success("Assignment submitted", submissionService.submitAssignment(assignmentId, dto));
    }

    @Operation(summary = "List submissions for an assignment", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'TA')")
    public ApiResponse<Page<AssignmentSubmissionVO>> listSubmissions(
            @PathVariable Long assignmentId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(submissionService.listSubmissions(assignmentId, pageable));
    }

    @Operation(summary = "Download all submissions for an assignment", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/assignments/{assignmentId}/download")
    @PreAuthorize("hasAnyRole('TEACHER', 'TA')")
    public ResponseEntity<Resource> downloadSubmissions(@PathVariable Long assignmentId) {
        Resource resource = submissionService.downloadAllSubmissions(assignmentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"assignment_" + assignmentId + "_submissions.zip\"")
                .body(resource);
    }

    @Operation(summary = "Get my submission for an assignment", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/assignments/{assignmentId}/my")
    public ApiResponse<AssignmentSubmissionVO> getMySubmission(@PathVariable Long assignmentId) {
        return ApiResponse.success(submissionService.getMySubmission(assignmentId));
    }

    @Operation(summary = "List my submissions for a course", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}/my")
    public ApiResponse<List<AssignmentSubmissionVO>> listMySubmissionsByCourse(@PathVariable Long courseId) {
        return ApiResponse.success(submissionService.listMySubmissionsByCourse(courseId));
    }

    @Operation(summary = "List submission counts for a course", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}/counts")
    @PreAuthorize("hasAnyRole('TEACHER', 'TA')")
    public ApiResponse<List<AssignmentSubmissionCountVO>> listSubmissionCountsByCourse(
            @PathVariable Long courseId) {
        return ApiResponse.success(submissionService.listSubmissionCountsByCourse(courseId));
    }

    @Operation(summary = "Get a submission by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{submissionId}")
    public ApiResponse<AssignmentSubmissionVO> getSubmission(@PathVariable Long submissionId) {
        return ApiResponse.success(submissionService.getSubmission(submissionId));
    }

    @Operation(summary = "Update a submission", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{submissionId}")
    public ApiResponse<AssignmentSubmissionVO> updateSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody AssignmentSubmissionDTO dto) {
        return ApiResponse.success("Submission updated", submissionService.updateSubmission(submissionId, dto));
    }

    @Operation(summary = "Grade a submission", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{submissionId}/grade")
    @PreAuthorize("hasAnyRole('TEACHER', 'TA')")
    public ApiResponse<AssignmentSubmissionVO> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestBody GradeSubmissionDTO dto) {
        return ApiResponse.success("Submission graded", submissionService.gradeSubmission(submissionId, dto));
    }
}
