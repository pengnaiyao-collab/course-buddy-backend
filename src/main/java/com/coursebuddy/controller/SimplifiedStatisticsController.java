package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.vo.SimplifiedStatsVO;
import com.coursebuddy.service.ICourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 控制器
 */
@Tag(name = "Simplified Statistics", description = "Simplified statistics management as per refactoring requirements")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class SimplifiedStatisticsController {

    private final ICourseService courseService;

    @Operation(summary = "Get course score statistics", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{courseId}/score")
    @PreAuthorize("hasAnyRole('TEACHER', 'TA')")
    public ApiResponse<SimplifiedStatsVO> getCourseScoreStats(@PathVariable Long courseId) {
        return ApiResponse.success(courseService.getSimplifiedCourseStats(courseId));
    }
}
