package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.NoteDTO;
import com.coursebuddy.domain.vo.NoteVO;
import com.coursebuddy.service.INoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notes", description = "Notes management endpoints")
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final INoteService service;

    @Operation(summary = "Create a note", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NoteVO> create(@Valid @RequestBody NoteDTO dto) {
        return ApiResponse.success("Note created successfully", service.create(dto));
    }

    @Operation(summary = "List my notes", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ApiResponse<Page<NoteVO>> list(
            @RequestParam(required = false) Long courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.listMyNotes(courseId, pageable));
    }

    @Operation(summary = "Get a note by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ApiResponse<NoteVO> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @Operation(summary = "Update a note", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ApiResponse<NoteVO> update(
            @PathVariable Long id,
            @Valid @RequestBody NoteDTO dto) {
        return ApiResponse.success(service.update(id, dto));
    }

    @Operation(summary = "Delete a note", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "Search notes by keyword", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/search")
    public ApiResponse<Page<NoteVO>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.search(keyword, pageable));
    }
}
