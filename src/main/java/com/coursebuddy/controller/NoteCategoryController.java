package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.NoteCategoryDTO;
import com.coursebuddy.domain.vo.NoteCategoryVO;
import com.coursebuddy.service.INoteCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记分类控制器
 */
@Tag(name = "Note Categories", description = "Note category management endpoints")
@RestController
@RequestMapping("/notes/categories")
@RequiredArgsConstructor
public class NoteCategoryController {

    private final INoteCategoryService service;

    @Operation(summary = "Create a note category", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NoteCategoryVO> create(@Valid @RequestBody NoteCategoryDTO dto) {
        return ApiResponse.success("Category created", service.create(dto));
    }

    @Operation(summary = "List my note categories", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ApiResponse<List<NoteCategoryVO>> list() {
        return ApiResponse.success(service.listMy());
    }

    @Operation(summary = "Update a note category", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ApiResponse<NoteCategoryVO> update(
            @PathVariable Long id,
            @Valid @RequestBody NoteCategoryDTO dto) {
        return ApiResponse.success(service.update(id, dto));
    }

    @Operation(summary = "Delete a note category", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
