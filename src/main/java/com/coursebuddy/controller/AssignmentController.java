package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.AssignmentDTO;
import com.coursebuddy.domain.vo.AssignmentVO;
import com.coursebuddy.service.IAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Assignments", description = "Assignment management endpoints")
@RestController
@RequestMapping("/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final IAssignmentService assignmentService;

    @Operation(summary = "Create an assignment", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/courses/{courseId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AssignmentVO> createAssignment(@PathVariable Long courseId, @Valid @RequestBody AssignmentDTO dto) {
        return ApiResponse.success("Assignment created", assignmentService.createAssignment(courseId, dto));
    }

    @Operation(summary = "List assignments for a course", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}")
    public ApiResponse<Page<AssignmentVO>> listAssignments(
            @PathVariable Long courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(assignmentService.listAssignments(courseId, pageable));
    }

    @Operation(summary = "Get an assignment by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{assignmentId}")
    public ApiResponse<AssignmentVO> getAssignment(@PathVariable Long assignmentId) {
        return ApiResponse.success(assignmentService.getAssignment(assignmentId));
    }

    @Operation(summary = "Update an assignment", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{assignmentId}")
    public ApiResponse<AssignmentVO> updateAssignment(@PathVariable Long assignmentId, @Valid @RequestBody AssignmentDTO dto) {
        return ApiResponse.success("Assignment updated", assignmentService.updateAssignment(assignmentId, dto));
    }

    @Operation(summary = "Delete an assignment", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{assignmentId}")
    public ApiResponse<Void> deleteAssignment(@PathVariable Long assignmentId) {
        assignmentService.deleteAssignment(assignmentId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "Publish an assignment", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{assignmentId}/publish")
    public ApiResponse<AssignmentVO> publishAssignment(@PathVariable Long assignmentId) {
        return ApiResponse.success("Assignment published", assignmentService.publishAssignment(assignmentId));
    }
}
