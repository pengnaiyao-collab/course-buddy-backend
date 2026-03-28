package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.KnowledgeAssociationDTO;
import com.coursebuddy.domain.vo.KnowledgeAssociationVO;
import com.coursebuddy.service.IKnowledgeAssociationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Knowledge Associations", description = "知识点关联管理接口")
@RestController
@RequestMapping("/courses/{courseId}/knowledge-base/{itemId}/associations")
@RequiredArgsConstructor
public class KnowledgeAssociationController {

    private final IKnowledgeAssociationService associationService;

    @Operation(summary = "创建知识点关联", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<KnowledgeAssociationVO> create(
            @PathVariable Long itemId,
            @Valid @RequestBody KnowledgeAssociationDTO dto) {
        return ApiResponse.success("关联创建成功", associationService.createAssociation(itemId, dto));
    }

    @Operation(summary = "获取知识点的所有关联", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ApiResponse<List<KnowledgeAssociationVO>> listBySource(@PathVariable Long itemId) {
        return ApiResponse.success(associationService.listBySource(itemId));
    }

    @Operation(summary = "获取指向该知识点的关联", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/incoming")
    public ApiResponse<List<KnowledgeAssociationVO>> listByTarget(@PathVariable Long itemId) {
        return ApiResponse.success(associationService.listByTarget(itemId));
    }

    @Operation(summary = "删除知识点关联", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{associationId}")
    public ApiResponse<Void> delete(@PathVariable Long associationId) {
        associationService.deleteAssociation(associationId);
        return ApiResponse.success(null);
    }
}
