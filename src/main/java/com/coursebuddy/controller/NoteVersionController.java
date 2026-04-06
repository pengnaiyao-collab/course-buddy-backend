package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.vo.NoteVersionVO;
import com.coursebuddy.service.INoteVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记版本控制器
 */
@Tag(name = "Note Versions", description = "Note version management endpoints")
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteVersionController {

    private final INoteVersionService service;

    @Operation(summary = "Save a version snapshot of a note", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{noteId}/versions")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NoteVersionVO> saveVersion(
            @PathVariable Long noteId,
            @RequestParam(required = false) String changeDesc) {
        return ApiResponse.success("Version saved", service.saveVersion(noteId, changeDesc));
    }

    @Operation(summary = "List versions of a note", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{noteId}/versions")
    public ApiResponse<List<NoteVersionVO>> listVersions(@PathVariable Long noteId) {
        return ApiResponse.success(service.listVersions(noteId));
    }

    @Operation(summary = "Get a specific version of a note", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{noteId}/versions/{versionNo}")
    public ApiResponse<NoteVersionVO> getVersion(
            @PathVariable Long noteId,
            @PathVariable Integer versionNo) {
        return ApiResponse.success(service.getVersion(noteId, versionNo));
    }

    @Operation(summary = "Restore a note to a specific version", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{noteId}/versions/{versionNo}/restore")
    public ApiResponse<Void> restoreVersion(
            @PathVariable Long noteId,
            @PathVariable Integer versionNo) {
        service.restoreVersion(noteId, versionNo);
        return ApiResponse.success(null);
    }
}
