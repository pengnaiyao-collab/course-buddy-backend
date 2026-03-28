package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.WebImportDTO;
import com.coursebuddy.domain.vo.WebImportVO;
import com.coursebuddy.service.IWebImportService;
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

@Tag(name = "Web Import", description = "网页链接导入知识库接口")
@RestController
@RequestMapping("/courses/{courseId}/web-imports")
@RequiredArgsConstructor
public class WebImportController {

    private final IWebImportService webImportService;

    @Operation(summary = "导入网页到知识库", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WebImportVO> importWebPage(
            @PathVariable Long courseId,
            @Valid @RequestBody WebImportDTO dto) {
        return ApiResponse.success("网页导入完成", webImportService.importWebPage(courseId, dto));
    }

    @Operation(summary = "获取课程网页导入列表", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ApiResponse<Page<WebImportVO>> list(
            @PathVariable Long courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(webImportService.listByCourse(courseId, pageable));
    }

    @Operation(summary = "获取网页导入详情", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ApiResponse<WebImportVO> getById(@PathVariable Long id) {
        return ApiResponse.success(webImportService.getById(id));
    }

    @Operation(summary = "删除网页导入记录", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        webImportService.delete(id);
        return ApiResponse.success(null);
    }
}
