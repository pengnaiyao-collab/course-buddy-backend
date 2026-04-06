package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.AttendanceMarkDTO;
import com.coursebuddy.domain.vo.AttendanceStatsVO;
import com.coursebuddy.domain.vo.AttendanceVO;
import com.coursebuddy.domain.vo.CheckInCodeVO;
import com.coursebuddy.service.IAttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤控制器
 */
@Tag(name = "Attendance", description = "Attendance management endpoints")
@RestController
@RequestMapping("/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final IAttendanceService attendanceService;

    @Operation(summary = "Mark attendance", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'TA')")
    public ApiResponse<Void> markAttendance(
            @PathVariable Long courseId,
            @RequestBody AttendanceMarkDTO dto) {
        attendanceService.markAttendance(courseId, dto.getSessionDate(), dto.getEntries());
        return ApiResponse.success(null);
    }

    @Operation(summary = "Get course attendance", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'TA')")
    public ApiResponse<Page<AttendanceVO>> getCourseAttendance(
            @PathVariable Long courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(attendanceService.getCourseAttendance(courseId, pageable));
    }

    @Operation(summary = "Get course attendance by date", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}/date")
    @PreAuthorize("hasAnyRole('TEACHER', 'TA')")
    public ApiResponse<List<AttendanceVO>> getCourseAttendanceByDate(
            @PathVariable Long courseId,
            @RequestParam("sessionDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate) {
        return ApiResponse.success(attendanceService.getCourseAttendanceByDate(courseId, sessionDate));
    }

    @Operation(summary = "Get my attendance for a course", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}/my")
    public ApiResponse<Page<AttendanceVO>> getMyAttendance(
            @PathVariable Long courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(attendanceService.getMyAttendance(courseId, pageable));
    }

    @Operation(summary = "Get my attendance by date", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}/my/date")
    public ApiResponse<List<AttendanceVO>> getMyAttendanceByDate(
            @PathVariable Long courseId,
            @RequestParam("sessionDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate) {
        return ApiResponse.success(attendanceService.getMyAttendanceByDate(courseId, sessionDate));
    }

    @Operation(summary = "Get my attendance statistics", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}/my-stats")
    public ApiResponse<AttendanceStatsVO> getMyAttendanceStats(@PathVariable Long courseId) {
        return ApiResponse.success(attendanceService.getMyAttendanceStats(courseId));
    }

    @Operation(summary = "Generate 6-digit check-in code", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/courses/{courseId}/code")
    @PreAuthorize("hasAnyRole('TEACHER', 'TA')")
    public ApiResponse<CheckInCodeVO> generateCheckInCode(@PathVariable Long courseId) {
        return ApiResponse.success(attendanceService.generateCheckInCode(courseId));
    }

    @Operation(summary = "Get current check-in code", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}/code")
    @PreAuthorize("hasAnyRole('TEACHER', 'TA')")
    public ApiResponse<CheckInCodeVO> getCheckInCode(@PathVariable Long courseId) {
        return ApiResponse.success(attendanceService.getCheckInCode(courseId));
    }

    @Operation(summary = "Check in with 6-digit code", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/courses/{courseId}/checkin")
    public ApiResponse<Void> checkIn(@PathVariable Long courseId, @RequestParam String code) {
        attendanceService.checkInWithCode(courseId, code);
        return ApiResponse.success(null);
    }
}
