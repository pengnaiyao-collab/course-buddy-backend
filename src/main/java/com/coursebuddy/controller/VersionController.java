package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.po.VersionPO;
import com.coursebuddy.domain.vo.VersionDiffVO;
import com.coursebuddy.service.IVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 版本控制器
 */
@Tag(name = "Version Management", description = "版本管理与历史对比接口")
@RestController
@RequestMapping("/v1/versions")
@RequiredArgsConstructor
public class VersionController {

    private final IVersionService versionService;

    @Operation(summary = "保存新版本", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ApiResponse<VersionPO> saveVersion(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @RequestParam String content,
            @RequestParam(required = false) String description) {
        return ApiResponse.success(
                versionService.saveVersion(entityType, entityId, content, description));
    }

    @Operation(summary = "获取版本列表", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ApiResponse<List<VersionPO>> listVersions(
            @RequestParam String entityType,
            @RequestParam Long entityId) {
        return ApiResponse.success(versionService.listVersions(entityType, entityId));
    }

    @Operation(summary = "获取指定版本内容", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{versionNumber}")
    public ApiResponse<VersionPO> getVersion(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @PathVariable int versionNumber) {
        return ApiResponse.success(
                versionService.getVersion(entityType, entityId, versionNumber));
    }

    @Operation(summary = "对比两个版本差异", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/compare")
    public ApiResponse<VersionDiffVO> compareVersions(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @RequestParam int versionA,
            @RequestParam int versionB) {
        return ApiResponse.success(
                versionService.compareVersions(entityType, entityId, versionA, versionB));
    }
}
