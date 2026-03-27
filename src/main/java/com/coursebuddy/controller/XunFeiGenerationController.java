package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.GenerateContentDTO;
import com.coursebuddy.domain.vo.GeneratedContentVO;
import com.coursebuddy.service.IContentGeneratorService;
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

@Tag(name = "AI Content Generation", description = "讯飞星火智能内容生成接口")
@RestController
@RequestMapping("/v1/ai/generate")
@RequiredArgsConstructor
public class XunFeiGenerationController {

    private final IContentGeneratorService contentGeneratorService;

    @Operation(summary = "生成复习提纲", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/outline")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GeneratedContentVO> generateOutline(@Valid @RequestBody GenerateContentDTO dto) {
        return ApiResponse.success("复习提纲生成成功", contentGeneratorService.generateOutline(dto));
    }

    @Operation(summary = "生成考点汇总", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/exam-points")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GeneratedContentVO> generateExamPoints(@Valid @RequestBody GenerateContentDTO dto) {
        return ApiResponse.success("考点汇总生成成功", contentGeneratorService.generateExamPoints(dto));
    }

    @Operation(summary = "生成习题", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GeneratedContentVO> generateQuestions(@Valid @RequestBody GenerateContentDTO dto) {
        return ApiResponse.success("习题生成成功", contentGeneratorService.generateQuestions(dto));
    }

    @Operation(summary = "知识点拆解", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/breakdown")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GeneratedContentVO> generateBreakdown(@Valid @RequestBody GenerateContentDTO dto) {
        return ApiResponse.success("知识点拆解成功", contentGeneratorService.generateBreakdown(dto));
    }

    @Operation(summary = "获取生成历史列表", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ApiResponse<Page<GeneratedContentVO>> listGeneratedContents(
            @RequestParam(required = false) String contentType,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(contentGeneratorService.listGeneratedContents(contentType, pageable));
    }

    @Operation(summary = "获取生成内容详情", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ApiResponse<GeneratedContentVO> getGeneratedContent(@PathVariable Long id) {
        return ApiResponse.success(contentGeneratorService.getGeneratedContent(id));
    }
}
