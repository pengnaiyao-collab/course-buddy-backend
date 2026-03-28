package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.CoursePermissionDTO;
import com.coursebuddy.domain.vo.CoursePermissionVO;
import com.coursebuddy.service.ICoursePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程权限管理 Controller
 * 四级权限: L1（管理员）> L2（核心协作）> L3（班级成员）> L4（访客）
 */
@Tag(name = "Course Permission", description = "课程权限管理接口")
@RestController
@RequestMapping("/v1/permissions")
@RequiredArgsConstructor
public class CoursePermissionController {

    private final ICoursePermissionService permissionService;

    @Operation(summary = "授予用户课程权限", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/grant")
    public ApiResponse<CoursePermissionVO> grant(@Valid @RequestBody CoursePermissionDTO dto) {
        return ApiResponse.success(permissionService.grantPermission(dto));
    }

    @Operation(summary = "更新用户课程权限级别", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/update")
    public ApiResponse<CoursePermissionVO> update(@Valid @RequestBody CoursePermissionDTO dto) {
        return ApiResponse.success(permissionService.updatePermission(dto));
    }

    @Operation(summary = "撤销用户课程权限", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/revoke")
    public ApiResponse<Void> revoke(
            @RequestParam Long userId,
            @RequestParam Long courseId) {
        permissionService.revokePermission(userId, courseId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取课程所有成员权限", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/course/{courseId}/members")
    public ApiResponse<List<CoursePermissionVO>> getCourseMembers(@PathVariable Long courseId) {
        return ApiResponse.success(permissionService.getCourseMembers(courseId));
    }

    @Operation(summary = "获取课程管理员列表", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/course/{courseId}/admins")
    public ApiResponse<List<CoursePermissionVO>> getCourseAdmins(@PathVariable Long courseId) {
        return ApiResponse.success(permissionService.getCourseAdmins(courseId));
    }

    @Operation(summary = "获取用户所有课程权限", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/user/{userId}")
    public ApiResponse<List<CoursePermissionVO>> getUserPermissions(@PathVariable Long userId) {
        return ApiResponse.success(permissionService.getUserPermissions(userId));
    }

    @Operation(summary = "获取当前用户在指定课程中的权限级别", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me/course/{courseId}")
    public ApiResponse<String> getMyPermission(@PathVariable Long courseId) {
        return ApiResponse.success(permissionService.getPermissionLevel(courseId));
    }

    @Operation(summary = "检查当前用户是否具有指定课程的最低权限", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/check")
    public ApiResponse<Boolean> checkPermission(
            @RequestParam Long courseId,
            @RequestParam(defaultValue = "L4") String minLevel) {
        return ApiResponse.success(permissionService.hasPermission(courseId, minLevel));
    }
}
