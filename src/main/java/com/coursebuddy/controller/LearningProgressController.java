package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.LearningProgressDTO;
import com.coursebuddy.domain.vo.LearningProgressVO;
import com.coursebuddy.service.ILearningProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 进度控制器
 */
@Tag(name = "Learning Progress", description = "Learning progress tracking endpoints")
@RestController
@RequestMapping("/learning-progress")
@RequiredArgsConstructor
public class LearningProgressController {

    private final ILearningProgressService service;

    @Operation(summary = "Update learning progress for a course", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping
    public ApiResponse<LearningProgressVO> updateProgress(@Valid @RequestBody LearningProgressDTO dto) {
        return ApiResponse.success("Progress updated", service.updateProgress(dto));
    }

    @Operation(summary = "Get my progress for a specific course", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}")
    public ApiResponse<LearningProgressVO> getProgressForCourse(@PathVariable Long courseId) {
        return ApiResponse.success(service.getMyProgressForCourse(courseId));
    }

    @Operation(summary = "List all my learning progress", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ApiResponse<Page<LearningProgressVO>> listMyProgress(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.listMyProgress(pageable));
    }

    @Operation(summary = "Get my learning stats for a course", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}/stats")
    public ApiResponse<Map<String, Object>> getCourseStats(@PathVariable Long courseId) {
        return ApiResponse.success(service.getMyCourseStats(courseId));
    }

    @Operation(summary = "Get average progress of all students in a course", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}/average")
    public ApiResponse<Map<String, Object>> getAverageProgress(@PathVariable Long courseId) {
        return ApiResponse.success(Map.of("averageProgress", service.getAverageCourseProgress(courseId)));
    }
}
