package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.NoteShareDTO;
import com.coursebuddy.domain.vo.NoteShareVO;
import com.coursebuddy.service.INoteShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Note Sharing", description = "Note sharing endpoints")
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteShareController {

    private final INoteShareService service;

    @Operation(summary = "Create a share link for a note", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{noteId}/shares")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NoteShareVO> createShare(
            @PathVariable Long noteId,
            @RequestBody(required = false) NoteShareDTO dto) {
        return ApiResponse.success("Share link created", service.createShare(noteId, dto != null ? dto : new NoteShareDTO()));
    }

    @Operation(summary = "List my note shares", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/shares")
    public ApiResponse<Page<NoteShareVO>> listMyShares(
            @RequestParam(required = false) Long noteId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.listMyShares(noteId, pageable));
    }

    @Operation(summary = "Access a shared note by token")
    @GetMapping("/public/{shareToken}")
    public ApiResponse<NoteShareVO> getByToken(@PathVariable String shareToken) {
        return ApiResponse.success(service.getByToken(shareToken));
    }

    @Operation(summary = "Update a share link", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/shares/{shareId}")
    public ApiResponse<NoteShareVO> updateShare(
            @PathVariable Long shareId,
            @RequestBody NoteShareDTO dto) {
        return ApiResponse.success(service.updateShare(shareId, dto));
    }

    @Operation(summary = "Delete a share link", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/shares/{shareId}")
    public ApiResponse<Void> deleteShare(@PathVariable Long shareId) {
        service.deleteShare(shareId);
        return ApiResponse.success(null);
    }
}
