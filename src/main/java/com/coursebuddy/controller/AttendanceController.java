package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.AttendanceMarkDTO;
import com.coursebuddy.domain.vo.AttendanceStatsVO;
import com.coursebuddy.domain.vo.AttendanceVO;
import com.coursebuddy.service.IAttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Attendance", description = "Attendance management endpoints")
@RestController
@RequestMapping("/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final IAttendanceService attendanceService;

    @Operation(summary = "Mark attendance", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Void> markAttendance(
            @PathVariable Long courseId,
            @RequestBody AttendanceMarkDTO dto) {
        attendanceService.markAttendance(courseId, dto.getSessionDate(), dto.getEntries());
        return ApiResponse.success(null);
    }

    @Operation(summary = "Get course attendance", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<Page<AttendanceVO>> getCourseAttendance(
            @PathVariable Long courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(attendanceService.getCourseAttendance(courseId, pageable));
    }

    @Operation(summary = "Get my attendance for a course", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}/my")
    public ApiResponse<Page<AttendanceVO>> getMyAttendance(
            @PathVariable Long courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(attendanceService.getMyAttendance(courseId, pageable));
    }

    @Operation(summary = "Get my attendance statistics", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}/my-stats")
    public ApiResponse<AttendanceStatsVO> getMyAttendanceStats(@PathVariable Long courseId) {
        return ApiResponse.success(attendanceService.getMyAttendanceStats(courseId));
    }
}
