package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.CourseDTO;
import com.coursebuddy.domain.vo.CourseStatsVO;
import com.coursebuddy.domain.vo.CourseVO;
import com.coursebuddy.service.ICourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Course Catalog", description = "Course catalog management endpoints")
@RestController
@RequestMapping("/v1/courses-catalog")
@RequiredArgsConstructor
public class CourseCatalogController {

    private final ICourseService courseService;

    @Operation(summary = "Create a new course", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<CourseVO> createCourse(@Valid @RequestBody CourseDTO dto) {
        return ApiResponse.success("Course created successfully", courseService.createCourse(dto));
    }

    @Operation(summary = "List all courses")
    @GetMapping
    public ApiResponse<Page<CourseVO>> listAllCourses(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(courseService.listAllCourses(pageable));
    }

    @Operation(summary = "Search courses")
    @GetMapping("/search")
    public ApiResponse<Page<CourseVO>> searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(courseService.searchCourses(keyword, level, pageable));
    }

    @Operation(summary = "List my teaching courses", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/my-teaching")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<Page<CourseVO>> listMyTeachingCourses(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(courseService.listMyTeachingCourses(pageable));
    }

    @Operation(summary = "Get a course by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{courseId}")
    public ApiResponse<CourseVO> getCourse(@PathVariable Long courseId) {
        return ApiResponse.success(courseService.getCourse(courseId));
    }

    @Operation(summary = "Update a course", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<CourseVO> updateCourse(@PathVariable Long courseId, @Valid @RequestBody CourseDTO dto) {
        return ApiResponse.success("Course updated successfully", courseService.updateCourse(courseId, dto));
    }

    @Operation(summary = "Delete a course", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "Get course statistics", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{courseId}/stats")
    public ApiResponse<CourseStatsVO> getCourseStats(@PathVariable Long courseId) {
        return ApiResponse.success(courseService.getCourseStats(courseId));
    }
}
