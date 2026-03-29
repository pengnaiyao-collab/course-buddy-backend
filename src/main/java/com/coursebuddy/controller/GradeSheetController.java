package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.GradeUpdateDTO;
import com.coursebuddy.domain.vo.GradeSheetVO;
import com.coursebuddy.service.IGradeSheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Grade Sheets", description = "Grade sheet management endpoints")
@RestController
@RequestMapping("/v1/grades")
@RequiredArgsConstructor
public class GradeSheetController {

    private final IGradeSheetService gradeSheetService;

    @Operation(summary = "List course grades", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Page<GradeSheetVO>> listCourseGrades(
            @PathVariable Long courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(gradeSheetService.listCourseGrades(courseId, pageable));
    }

    @Operation(summary = "Get my grade for a course", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}/my")
    public ApiResponse<GradeSheetVO> getMyGrade(@PathVariable Long courseId) {
        return ApiResponse.success(gradeSheetService.getMyGrade(courseId));
    }

    @Operation(summary = "Update grade sheet for a student", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/courses/{courseId}/students/{studentId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<GradeSheetVO> updateGradeSheet(
            @PathVariable Long courseId,
            @PathVariable Long studentId,
            @RequestBody GradeUpdateDTO dto) {
        return ApiResponse.success("Grade sheet updated", gradeSheetService.updateGradeSheet(courseId, studentId, dto));
    }
}
