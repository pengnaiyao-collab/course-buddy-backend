package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.KnowledgeAnalyzeDTO;
import com.coursebuddy.domain.dto.KnowledgeItemDTO;
import com.coursebuddy.domain.dto.KnowledgeResourceDTO;
import com.coursebuddy.domain.vo.KnowledgeAnalyzeResultVO;
import com.coursebuddy.domain.vo.KnowledgeGraphVO;
import com.coursebuddy.domain.vo.KnowledgeItemVO;
import com.coursebuddy.domain.vo.KnowledgeResourceVO;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    @Operation(summary = "Advanced search knowledge items (keyword + tag combinations)", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/search/advanced")
    public ApiResponse<Page<KnowledgeItemVO>> advancedSearch(
            @PathVariable Long courseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tags,
            @RequestParam(defaultValue = "OR") String tagMode,
            @PageableDefault(size = 20) Pageable pageable) {
        List<String> tagList = (tags == null || tags.isBlank())
                ? Collections.emptyList()
                : Arrays.stream(tags.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
        return ApiResponse.success(service.searchAdvanced(courseId, keyword, tagList, tagMode, pageable));
    }

    @Operation(summary = "Auto analyze long document text and generate structured knowledge items", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/auto-analyze")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<KnowledgeAnalyzeResultVO> autoAnalyze(
            @PathVariable Long courseId,
            @Valid @RequestBody KnowledgeAnalyzeDTO dto) {
        return ApiResponse.success("Long document analyzed successfully",
                service.autoAnalyze(courseId, dto));
    }

    @Operation(summary = "Generate structured knowledge graph", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/graph")
    public ApiResponse<KnowledgeGraphVO> getGraph(@PathVariable Long courseId) {
        return ApiResponse.success(service.buildGraph(courseId));
    }

    @Operation(summary = "Generate mind map in Mermaid format for a knowledge point", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}/mindmap")
    public ApiResponse<String> generateMindMap(
            @PathVariable Long courseId,
            @PathVariable Long id) {
        return ApiResponse.success(service.generateMindMap(courseId, id));
    }

    @Operation(summary = "Attach a resource to a knowledge point", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/resources")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<KnowledgeResourceVO> addResource(
            @PathVariable Long courseId,
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeResourceDTO dto) {
        return ApiResponse.success("Resource attached successfully",
                service.addResource(courseId, id, dto));
    }

    @Operation(summary = "List attached resources for a knowledge point", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}/resources")
    public ApiResponse<List<KnowledgeResourceVO>> listResources(
            @PathVariable Long courseId,
            @PathVariable Long id) {
        return ApiResponse.success(service.listResources(courseId, id));
    }

    @Operation(summary = "Delete an attached resource from a knowledge point", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}/resources/{resourceId}")
    public ApiResponse<Void> deleteResource(
            @PathVariable Long courseId,
            @PathVariable Long id,
            @PathVariable Long resourceId) {
        service.deleteResource(courseId, id, resourceId);
        return ApiResponse.success(null);
    }
}
