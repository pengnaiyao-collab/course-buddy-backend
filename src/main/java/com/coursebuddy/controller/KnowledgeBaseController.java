package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.KnowledgeItemDTO;
import com.coursebuddy.domain.vo.KnowledgeItemVO;
import com.coursebuddy.service.IKnowledgeBaseService;
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

@Tag(name = "Knowledge Base", description = "Knowledge base management endpoints")
@RestController
@RequestMapping("/courses/{courseId}/knowledge-base")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final IKnowledgeBaseService service;

    @Operation(summary = "Create a knowledge item", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<KnowledgeItemVO> create(
            @PathVariable Long courseId,
            @Valid @RequestBody KnowledgeItemDTO dto) {
        return ApiResponse.success("Knowledge item created successfully",
                service.createForCourse(courseId, dto));
    }

    @Operation(summary = "List knowledge items for a course", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ApiResponse<Page<KnowledgeItemVO>> list(
            @PathVariable Long courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.listByCourse(courseId, pageable));
    }

    @Operation(summary = "Get a knowledge item by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ApiResponse<KnowledgeItemVO> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @Operation(summary = "Update a knowledge item", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ApiResponse<KnowledgeItemVO> update(
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeItemDTO dto) {
        return ApiResponse.success(service.update(id, dto));
    }

    @Operation(summary = "Delete a knowledge item", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "Search knowledge items", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/search")
    public ApiResponse<Page<KnowledgeItemVO>> search(
            @PathVariable Long courseId,
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.search(courseId, keyword, pageable));
    }
}
