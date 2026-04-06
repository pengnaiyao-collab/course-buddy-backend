package com.coursebuddy.controller;

import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.domain.dto.InitUploadRequest;
import com.coursebuddy.domain.po.FileUploadPO;
import com.coursebuddy.domain.vo.BatchUploadResultVO;
import com.coursebuddy.mapper.FileUploadMapper;
import com.coursebuddy.service.IMinIOUploadService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping({"/v1/files", "/files"})
@RequiredArgsConstructor
@Slf4j
public class MinIOFileUploadController {

    private final IMinIOUploadService uploadService;
    private final FileUploadMapper fileUploadRepository;

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

    @GetMapping
    public ApiResponse<Object> listFiles() {
        // 当前未实现文件列表，先返回空列表避免前端 404
        return ApiResponse.success(List.of());
    }

    @PostMapping("/upload/cancel/{sessionId}")
    public ApiResponse<Void> cancelUpload(@PathVariable String sessionId) {
        uploadService.cancelUpload(sessionId);
        return ApiResponse.success("上传已取消", null);
    }

    /**
     * 下载文件。objectName 通过请求参数传递，以支持包含斜杠的对象名
     * （例如 uploads/2024/01/15/uuid/file.pdf）。
     */
    @GetMapping("/download")
    public void downloadFile(@RequestParam String objectName,
                              @RequestParam(value = "preview", required = false, defaultValue = "false") boolean preview,
                              HttpServletResponse response) throws Exception {
        validateObjectName(objectName);
        FileUploadPO metadata = fileUploadRepository.findByObjectNameAndIsDeletedFalse(objectName).orElse(null);
        writeFileToResponse(objectName, metadata, preview, response);
    }

    @GetMapping("/avatar")
    public void previewAvatar(@RequestParam String objectName,
                              @RequestParam(value = "preview", required = false, defaultValue = "true") boolean preview,
                              HttpServletResponse response) throws Exception {
        validateObjectName(objectName);
        FileUploadPO metadata = fileUploadRepository.findByObjectNameAndIsDeletedFalse(objectName).orElse(null);
        if (metadata == null || metadata.getCategory() == null
                || !"avatar".equalsIgnoreCase(metadata.getCategory())) {
            throw new BusinessException(404, "头像文件不存在");
        }
        if (metadata.getContentType() == null || !metadata.getContentType().startsWith("image/")) {
            throw new BusinessException(404, "头像文件不存在");
        }
        writeFileToResponse(objectName, metadata, preview, response);
    }

    /**
     * 删除文件。objectName 通过请求参数传递。
     */
    @DeleteMapping
    public ApiResponse<Void> deleteFile(@RequestParam String objectName) throws Exception {
        // 验证用户是否已认证
        var currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(401, "User not authenticated");
        }
        
        validateObjectName(objectName);
        uploadService.deleteFile(objectName);
        return ApiResponse.success("文件删除成功", null);
    }

    /**
     * 获取预签名预览 URL。objectName 通过请求参数传递。
     */
    @GetMapping("/preview")
    public ApiResponse<String> getPreviewUrl(@RequestParam String objectName) throws Exception {
        validateObjectName(objectName);
        return ApiResponse.success(uploadService.getPreviewUrl(objectName));
    }

    @PostMapping("/upload/batch")
    public ApiResponse<BatchUploadResultVO> batchUpload(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "category", required = false) String category) {
        if (category != null && "avatar".equalsIgnoreCase(category)) {
            throw new BusinessException(400, "请使用头像上传接口");
        }
        log.info("Batch upload: {} files", files.length);
        return ApiResponse.success("批量上传完成", uploadService.batchUpload(files, category));
    }

    private void validateObjectName(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            throw new BusinessException("objectName 不能为空");
        }
        try {
            // 先进行 URL 解码
            String decoded = URLDecoder.decode(objectName, StandardCharsets.UTF_8);
            
            // 规范化路径，防止目录遍历
            java.nio.file.Path normalized = Paths.get(decoded).normalize();
            String normalizedPath = normalized.toString();
            
            // 确保路径以允许的前缀开头
            if (!normalizedPath.startsWith("uploads") && !normalizedPath.startsWith("uploads/")) {
                throw new BusinessException("非法的文件路径");
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的文件路径格式");
        }
    }

    private String sanitizeFileName(String objectName) {
        // 仅取最后一个路径片段作为下载文件名
        int lastSlash = objectName.lastIndexOf('/');
        return lastSlash >= 0 ? objectName.substring(lastSlash + 1) : objectName;
    }

    private void writeFileToResponse(String objectName,
                                     FileUploadPO metadata,
                                     boolean preview,
                                     HttpServletResponse response) throws Exception {
        String safeFileName = metadata != null && metadata.getFileName() != null
            ? metadata.getFileName()
            : sanitizeFileName(objectName);
        String encoded = URLEncoder.encode(safeFileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        try (InputStream stream = uploadService.downloadFile(objectName)) {
            String contentType = metadata != null ? metadata.getContentType() : null;
            if (contentType == null || contentType.isBlank()) {
                contentType = URLConnection.guessContentTypeFromName(safeFileName);
            }
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }
            if (contentType.startsWith("text/")) {
                contentType = contentType + "; charset=UTF-8";
            }
            response.setContentType(contentType);
            response.setHeader("Cache-Control", "no-cache");

            if (preview) {
                response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encoded);
            } else {
                response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            }

            byte[] buffer = new byte[1024 * 4];
            int bytesRead;
            while ((bytesRead = stream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("Download file error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
