package com.coursebuddy.controller;

import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.InitUploadRequest;
import com.coursebuddy.domain.vo.BatchUploadResultVO;
import com.coursebuddy.service.IMinIOUploadService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
@Slf4j
public class MinIOFileUploadController {

    private final IMinIOUploadService uploadService;

    @PostMapping("/upload/init")
    public ApiResponse<Object> initUpload(@Valid @RequestBody InitUploadRequest request) {
        log.info("Init upload: {}", request.getFileName());
        return ApiResponse.success("上传初始化成功", uploadService.initUpload(request));
    }

    @PostMapping("/upload/chunk")
    public ApiResponse<Object> uploadChunk(
            @RequestParam String sessionId,
            @RequestParam int chunkIndex,
            @RequestParam MultipartFile chunk) {
        log.info("Upload chunk: session={}, index={}", sessionId, chunkIndex);
        return ApiResponse.success("分片上传成功",
                uploadService.uploadChunk(sessionId, chunkIndex, chunk));
    }

    @PostMapping("/upload/merge")
    public ApiResponse<Object> mergeChunks(
            @RequestParam String sessionId,
            @RequestParam int totalChunks) {
        log.info("Merge chunks: session={}, total={}", sessionId, totalChunks);
        return ApiResponse.success("分片合并成功",
                uploadService.mergeChunks(sessionId, totalChunks));
    }

    @GetMapping("/upload/progress/{sessionId}")
    public ApiResponse<Object> getProgress(@PathVariable String sessionId) {
        return ApiResponse.success(uploadService.getProgress(sessionId));
    }

    @PostMapping("/upload/cancel/{sessionId}")
    public ApiResponse<Void> cancelUpload(@PathVariable String sessionId) {
        uploadService.cancelUpload(sessionId);
        return ApiResponse.success("上传已取消", null);
    }

    /**
     * Download a file. objectName is passed as a request parameter to support
     * names that contain forward slashes (e.g. uploads/2024/01/15/uuid/file.pdf).
     */
    @GetMapping("/download")
    public void downloadFile(@RequestParam String objectName,
                              HttpServletResponse response) throws Exception {
        validateObjectName(objectName);
        String safeFileName = sanitizeFileName(objectName);
        String encoded = URLEncoder.encode(safeFileName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        try (InputStream stream = uploadService.downloadFile(objectName)) {
            IOUtils.copy(stream, response.getOutputStream());
        }
    }

    /**
     * Delete a file. objectName is passed as a request parameter.
     */
    @DeleteMapping
    public ApiResponse<Void> deleteFile(@RequestParam String objectName) throws Exception {
        validateObjectName(objectName);
        uploadService.deleteFile(objectName);
        return ApiResponse.success("文件删除成功", null);
    }

    /**
     * Get a presigned preview URL. objectName is passed as a request parameter.
     */
    @GetMapping("/preview")
    public ApiResponse<String> getPreviewUrl(@RequestParam String objectName) throws Exception {
        validateObjectName(objectName);
        return ApiResponse.success(uploadService.getPreviewUrl(objectName));
    }

    @PostMapping("/upload/batch")
    public ApiResponse<BatchUploadResultVO> batchUpload(
            @RequestParam("files") MultipartFile[] files) {
        log.info("Batch upload: {} files", files.length);
        return ApiResponse.success("批量上传完成", uploadService.batchUpload(files));
    }

    private void validateObjectName(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            throw new BusinessException("objectName 不能为空");
        }
        // Prevent path traversal: only allow names starting with the uploads/ prefix
        // and reject any ".." segments
        if (objectName.contains("..") || !objectName.startsWith("uploads/")) {
            throw new BusinessException("非法的文件路径");
        }
    }

    private String sanitizeFileName(String objectName) {
        // Extract only the last path segment as the download filename
        int lastSlash = objectName.lastIndexOf('/');
        return lastSlash >= 0 ? objectName.substring(lastSlash + 1) : objectName;
    }
}
