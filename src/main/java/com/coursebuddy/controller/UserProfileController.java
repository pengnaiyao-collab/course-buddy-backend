package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.UserProfileDTO;
import com.coursebuddy.domain.vo.UserProfileVO;
import com.coursebuddy.service.IUserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户资料控制器
 */
@Tag(name = "User Profile", description = "User profile and settings endpoints")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final IUserProfileService service;

    @Operation(summary = "Get my profile", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ApiResponse<UserProfileVO> getMyProfile() {
        return ApiResponse.success(service.getMyProfile());
    }

    @Operation(summary = "Update my profile", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/me")
    public ApiResponse<UserProfileVO> updateMyProfile(@Valid @RequestBody UserProfileDTO dto) {
        return ApiResponse.success("Profile updated", service.updateMyProfile(dto));
    }

    @Operation(summary = "Update my avatar", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/me/avatar")
    public ApiResponse<UserProfileVO> updateMyAvatar(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success("Avatar updated", service.updateMyAvatar(file));
    }

    @Operation(summary = "Get a user's profile by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{userId}/profile")
    public ApiResponse<UserProfileVO> getProfileById(@PathVariable Long userId) {
        return ApiResponse.success(service.getProfileById(userId));
    }

    @Operation(summary = "Search users by keyword", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/search")
    public ApiResponse<Page<UserProfileVO>> searchUsers(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.searchUsers(keyword, status, role, pageable));
    }

    @Operation(summary = "Get pending teachers for review", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/pending-teachers")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<UserProfileVO>> getPendingTeachers(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.getPendingTeachers(pageable));
    }

    @Operation(summary = "Approve teacher registration", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{userId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> approveTeacher(@PathVariable Long userId) {
        service.approveTeacher(userId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "Reject teacher registration", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{userId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> rejectTeacher(@PathVariable Long userId) {
        service.rejectTeacher(userId);
        return ApiResponse.success(null);
    }

}
