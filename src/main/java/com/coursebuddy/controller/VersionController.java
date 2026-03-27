package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.po.VersionPO;
import com.coursebuddy.service.IVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/versions")
@RequiredArgsConstructor
public class VersionController {

    private final IVersionService versionService;

    @PostMapping
    public ApiResponse<VersionPO> saveVersion(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @RequestParam String content,
            @RequestParam(required = false) String description) {
        return ApiResponse.success(
                versionService.saveVersion(entityType, entityId, content, description));
    }

    @GetMapping
    public ApiResponse<List<VersionPO>> listVersions(
            @RequestParam String entityType,
            @RequestParam Long entityId) {
        return ApiResponse.success(versionService.listVersions(entityType, entityId));
    }

    @GetMapping("/{versionNumber}")
    public ApiResponse<VersionPO> getVersion(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @PathVariable int versionNumber) {
        return ApiResponse.success(
                versionService.getVersion(entityType, entityId, versionNumber));
    }
}
