package com.coursebuddy.notes;

import com.coursebuddy.common.ApiResponse;
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

    private final NoteService noteService;

    @Operation(summary = "Create a note", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NoteResponse> create(@Valid @RequestBody NoteRequest request) {
        return ApiResponse.success("Note created successfully", noteService.create(request));
    }

    @Operation(summary = "List my notes", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ApiResponse<Page<NoteResponse>> list(
            @RequestParam(required = false) Long courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(noteService.listMyNotes(courseId, pageable));
    }

    @Operation(summary = "Get a note by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ApiResponse<NoteResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(noteService.getById(id));
    }

    @Operation(summary = "Update a note", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ApiResponse<NoteResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody NoteRequest request) {
        return ApiResponse.success(noteService.update(id, request));
    }

    @Operation(summary = "Delete a note", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        noteService.delete(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "Search notes by keyword", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/search")
    public ApiResponse<Page<NoteResponse>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(noteService.search(keyword, pageable));
    }
}
