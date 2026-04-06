package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.CourseEnrollmentDTO;
import com.coursebuddy.domain.vo.CourseEnrollmentVO;
import com.coursebuddy.service.ICourseEnrollmentService;
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

import java.util.Map;

/**
 * 课程选课控制器
 */
@Tag(name = "Course Enrollment", description = "Course enrollment management endpoints")
@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class CourseEnrollmentController {

    private final ICourseEnrollmentService service;

    @Operation(summary = "Enroll in a course", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CourseEnrollmentVO> enroll(@Valid @RequestBody CourseEnrollmentDTO dto) {
        return ApiResponse.success("Enrolled successfully", service.enroll(dto));
    }

    @Operation(summary = "Enroll in a course by code", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/join")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CourseEnrollmentVO> joinCourse(@RequestParam String code) {
        return ApiResponse.success("成功加入课堂", service.enrollByCode(code));
    }

    @Operation(summary = "Drop a course", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/courses/{courseId}/drop")
    public ApiResponse<Void> drop(@PathVariable Long courseId) {
        service.drop(courseId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "Mark a course as completed", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/courses/{courseId}/complete")
    public ApiResponse<Void> complete(@PathVariable Long courseId) {
        service.complete(courseId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "List my enrollments", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ApiResponse<Page<CourseEnrollmentVO>> listMyEnrollments(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.listMyEnrollments(status, pageable));
    }

    @Operation(summary = "List students enrolled in a course", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}/students")
    public ApiResponse<Page<CourseEnrollmentVO>> listCourseStudents(
            @PathVariable Long courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.listCourseStudents(courseId, pageable));
    }

    @Operation(summary = "Check if I am enrolled in a course", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}/status")
    public ApiResponse<Map<String, Object>> checkEnrollment(@PathVariable Long courseId) {
        boolean enrolled = service.isEnrolled(courseId);
        long total = service.countCourseStudents(courseId);
        return ApiResponse.success(Map.of("enrolled", enrolled, "totalStudents", total));
    }
}
