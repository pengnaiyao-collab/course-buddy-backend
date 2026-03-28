package com.coursebuddy.controller;

import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.po.OcrResultPO;
import com.coursebuddy.domain.vo.OcrResultVO;
import com.coursebuddy.repository.OcrResultRepository;
import com.coursebuddy.service.IOcrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Tag(name = "OCR", description = "图片文字识别接口")
@RestController
@RequestMapping("/v1/ocr")
@RequiredArgsConstructor
@Slf4j
public class OcrController {

    private final IOcrService ocrService;
    private final OcrResultRepository ocrResultRepository;

    @Operation(summary = "识别上传图片中的文字", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/recognize")
    public ApiResponse<OcrResultVO> recognize(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "language", defaultValue = "chi_sim+eng") String language) {
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

        OcrResultPO saved = ocrResultRepository.save(OcrResultPO.builder()
                .objectName("ocr/direct/" + filename)
                .extractedText(extractedText)
                .language(language)
                .status(status)
                .errorMessage(errorMessage)
                .createdBy(userId)
                .build());

        return ApiResponse.success("OCR识别完成", OcrResultVO.builder()
                .id(saved.getId())
                .objectName(saved.getObjectName())
                .extractedText(extractedText)
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
        OcrResultPO po = ocrResultRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "OCR result not found"));
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
}
