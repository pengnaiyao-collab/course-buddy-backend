package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.NoteTagDTO;
import com.coursebuddy.domain.vo.NoteTagVO;
import com.coursebuddy.service.INoteTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Note Tags", description = "Note tag management endpoints")
@RestController
@RequestMapping("/notes/tags")
@RequiredArgsConstructor
public class NoteTagController {

    private final INoteTagService service;

    @Operation(summary = "Create a note tag", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NoteTagVO> create(@Valid @RequestBody NoteTagDTO dto) {
        return ApiResponse.success("Tag created", service.create(dto));
    }

    @Operation(summary = "List my note tags", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ApiResponse<List<NoteTagVO>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(service.listMy(keyword));
    }

    @Operation(summary = "Update a note tag", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ApiResponse<NoteTagVO> update(
            @PathVariable Long id,
            @Valid @RequestBody NoteTagDTO dto) {
        return ApiResponse.success(service.update(id, dto));
    }

    @Operation(summary = "Delete a note tag", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
