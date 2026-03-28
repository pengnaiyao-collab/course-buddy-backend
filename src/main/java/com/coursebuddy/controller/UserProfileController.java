package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.UserProfileDTO;
import com.coursebuddy.domain.dto.UserSettingsDTO;
import com.coursebuddy.domain.vo.UserProfileVO;
import com.coursebuddy.domain.vo.UserSettingsVO;
import com.coursebuddy.service.IUserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Get a user's profile by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{userId}/profile")
    public ApiResponse<UserProfileVO> getProfileById(@PathVariable Long userId) {
        return ApiResponse.success(service.getProfileById(userId));
    }

    @Operation(summary = "Search users by keyword", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/search")
    public ApiResponse<Page<UserProfileVO>> searchUsers(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.searchUsers(keyword, pageable));
    }

    @Operation(summary = "Get my settings", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me/settings")
    public ApiResponse<UserSettingsVO> getMySettings() {
        return ApiResponse.success(service.getMySettings());
    }

    @Operation(summary = "Update my settings", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/me/settings")
    public ApiResponse<UserSettingsVO> updateMySettings(@RequestBody UserSettingsDTO dto) {
        return ApiResponse.success("Settings updated", service.updateMySettings(dto));
    }
}
