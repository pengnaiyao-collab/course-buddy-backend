package com.coursebuddy.controller;

import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.KnowledgeItemDTO;
import com.coursebuddy.domain.po.OcrResultPO;
import com.coursebuddy.domain.vo.KnowledgeItemVO;
import com.coursebuddy.domain.vo.OcrResultVO;
import com.coursebuddy.mapper.OcrResultMapper;
import com.coursebuddy.service.IKnowledgeBaseService;
import com.coursebuddy.service.IOcrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

@Tag(name = "OCR", description = "图片文字识别接口")
@RestController
@RequestMapping("/v1/ocr")
@RequiredArgsConstructor
@Slf4j
public class OcrController {

    private final IOcrService ocrService;
    private final OcrResultMapper ocrResultRepository;
    private final IKnowledgeBaseService knowledgeBaseService;

    @Operation(summary = "识别上传图片中的文字", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/recognize")
    public ApiResponse<OcrResultVO> recognize(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "language", defaultValue = "chi_sim+eng") String language,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "autoArchive", defaultValue = "true") boolean autoArchive) {
        Long userId = null;
        try {
            userId = SecurityUtils.getCurrentUser().getId();
        } catch (Exception ignored) {}

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image";
        String extractedText = "";
        String status = "COMPLETED";
        String errorMessage = null;

        try (InputStream is = file.getInputStream()) {
            extractedText = ocrService.extractTextFromImage(is, filename, language);
        } catch (Exception e) {
            log.error("OCR recognition failed: {}", e.getMessage(), e);
            status = "FAILED";
            errorMessage = e.getMessage();
        }

        OcrResultPO saved = OcrResultPO.builder()
                .objectName("ocr/direct/" + filename)
                .extractedText(extractedText)
                .language(language)
                .status(status)
                .errorMessage(errorMessage)
                .createdBy(userId)
                .build();
        ocrResultRepository.insert(saved);

        Long knowledgeItemId = null;
        String structuredSummary = null;
        if ("COMPLETED".equals(status) && autoArchive && courseId != null
                && extractedText != null && !extractedText.isBlank()) {
            try {
                structuredSummary = buildStructuredSummary(extractedText);
                String resolvedCategory = (category == null || category.isBlank())
                        ? "OCR Notes" : category;
                KnowledgeItemDTO dto = KnowledgeItemDTO.builder()
                        .title("OCR - " + filename)
                        .description(structuredSummary)
                        .fileType("OCR")
                        .category(resolvedCategory)
                        .tags(normalizeTags(tags))
                        .build();
                KnowledgeItemVO created = knowledgeBaseService.createForCourse(courseId, dto);
                knowledgeItemId = created.getId();
            } catch (Exception e) {
                log.warn("Auto archive OCR result failed for file {}: {}", filename, e.getMessage());
            }
        }

        return ApiResponse.success("OCR识别完成", OcrResultVO.builder()
                .id(saved.getId())
                .objectName(saved.getObjectName())
                .extractedText(extractedText)
                .structuredSummary(structuredSummary)
                .knowledgeItemId(knowledgeItemId)
                .language(language)
                .status(status)
                .errorMessage(errorMessage)
                .createdBy(userId)
                .createdAt(saved.getCreatedAt())
                .ocrAvailable(ocrService.isAvailable())
                .build());
    }

    @Operation(summary = "查询OCR识别结果", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ApiResponse<OcrResultVO> getResult(@PathVariable Long id) {
        OcrResultPO po = ocrResultRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "OCR result not found");
        }
        return ApiResponse.success(OcrResultVO.builder()
                .id(po.getId())
                .fileUploadId(po.getFileUploadId())
                .objectName(po.getObjectName())
                .extractedText(po.getExtractedText())
                .confidence(po.getConfidence())
                .language(po.getLanguage())
                .status(po.getStatus())
                .errorMessage(po.getErrorMessage())
                .createdBy(po.getCreatedBy())
                .createdAt(po.getCreatedAt())
                .ocrAvailable(ocrService.isAvailable())
                .build());
    }

    @Operation(summary = "检查OCR服务是否可用")
    @GetMapping("/status")
    public ApiResponse<Boolean> checkStatus() {
        return ApiResponse.success("OCR服务状态", ocrService.isAvailable());
    }

    private String normalizeTags(String tags) {
        if (tags == null || tags.isBlank()) return "OCR";
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.joining(","));
    }

    private String buildStructuredSummary(String text) {
        String[] rawLines = text.split("\\r?\\n");
        var lines = Arrays.stream(rawLines)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .limit(24)
                .toList();

        if (lines.isEmpty()) {
            return "## OCR 结构化摘要\n\n- 未识别到有效文本";
        }

        String title = lines.get(0);
        StringBuilder sb = new StringBuilder();
        sb.append("## OCR 结构化摘要\n\n");
        sb.append("### 标题\n");
        sb.append("- ").append(title).append("\n\n");
        sb.append("### 关键内容\n");
        for (int i = 1; i < lines.size(); i++) {
            sb.append("- ").append(lines.get(i)).append("\n");
        }
        if (lines.size() == 1) {
            sb.append("- （无更多内容）\n");
        }
        return sb.toString();
    }
}
