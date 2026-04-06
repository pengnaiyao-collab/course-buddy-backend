package com.coursebuddy.service.impl;

import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.config.MinIOProperties;
import com.coursebuddy.domain.dto.InitUploadRequest;
import com.coursebuddy.domain.po.FileUploadPO;
import com.coursebuddy.domain.vo.BatchUploadResultVO;
import com.coursebuddy.domain.vo.ChunkUploadResponse;
import com.coursebuddy.domain.vo.FileUploadResponse;
import com.coursebuddy.domain.vo.InitUploadResponse;
import com.coursebuddy.domain.vo.UploadProgressVO;
import com.coursebuddy.mapper.FileUploadMapper;
import com.coursebuddy.service.IMinIOUploadService;
import com.coursebuddy.util.UploadSession;
import com.coursebuddy.util.UploadSessionManager;
import io.minio.BucketExistsArgs;
import io.minio.ComposeObjectArgs;
import io.minio.ComposeSource;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 上传服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MinIOUploadServiceImpl implements IMinIOUploadService {

    private final MinioClient minioClient;
    private final MinIOProperties properties;
    private final UploadSessionManager sessionManager;
    private final FileUploadMapper fileUploadRepository;

    @Override
    public InitUploadResponse initUpload(InitUploadRequest request) {
        if (request.getFileSize() > properties.getMaxFileSize()) {
            throw new BusinessException("文件过大，超过 "
                    + (properties.getMaxFileSize() / 1024 / 1024) + " MB");
        }

        int totalChunks = (int) Math.ceil(
                (double) request.getFileSize() / properties.getChunkSize());
        if (totalChunks == 0) totalChunks = 1;

        String objectName = UploadSessionManager.generateObjectName(request.getFileName());
        String sessionId = sessionManager.createSession(
                request.getFileName(), request.getFileSize(), request.getCategory(), totalChunks, objectName);

        return InitUploadResponse.builder()
                .sessionId(sessionId)
                .objectName(objectName)
                .chunkSize(properties.getChunkSize())
                .totalChunks(totalChunks)
                .build();
    }

    @Override
    public ChunkUploadResponse uploadChunk(String sessionId, int chunkIndex, MultipartFile chunk) {
        try {
            UploadSession session = sessionManager.getSession(sessionId);
            if (session == null) {
                throw new BusinessException("上传会话已过期");
            }

            String chunkObjectName = session.getObjectName() + ".chunk" + chunkIndex;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(chunkObjectName)
                    .stream(chunk.getInputStream(), chunk.getSize(), -1)
                    .contentType(chunk.getContentType() != null
                            ? chunk.getContentType() : "application/octet-stream")
                    .build());

            sessionManager.updateChunkProgress(sessionId, chunkIndex);

            log.info("Chunk {} uploaded for session {}", chunkIndex, sessionId);

            return ChunkUploadResponse.builder()
                    .sessionId(sessionId)
                    .chunkIndex(chunkIndex)
                    .status("success")
                    .progress(session.getProgress())
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error uploading chunk {}", chunkIndex, e);
            throw new BusinessException("分片上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public FileUploadResponse mergeChunks(String sessionId, int totalChunks) {
        try {
            UploadSession session = sessionManager.getSession(sessionId);
            if (session == null) {
                throw new BusinessException("上传会话已过期");
            }

            String objectName = session.getObjectName();
            List<ComposeSource> sources = new ArrayList<>();
            for (int i = 0; i < totalChunks; i++) {
                sources.add(ComposeSource.builder()
                        .bucket(properties.getBucketName())
                        .object(objectName + ".chunk" + i)
                        .build());
            }

            minioClient.composeObject(ComposeObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(objectName)
                    .sources(sources)
                    .build());

            // 删除分片对象
            for (int i = 0; i < totalChunks; i++) {
                try {
                    minioClient.removeObject(RemoveObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName + ".chunk" + i)
                            .build());
                } catch (Exception e) {
                    log.warn("Failed to delete chunk {}: {}", i, e.getMessage());
                }
            }

            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(objectName)
                    .build());

            FileUploadPO fileUpload = FileUploadPO.builder()
                    .objectName(objectName)
                    .fileName(session.getFileName())
                    .fileSize(stat.size())
                    .contentType(stat.contentType())
                    .category(session.getCategory())
                    .uploadUrl(buildFileUrl(objectName))
                    .uploadedAt(LocalDateTime.now())
                    .build();

            fileUploadRepository.insert(fileUpload);
            FileUploadPO saved = fileUpload;

            sessionManager.removeSession(sessionId);

            log.info("File merged successfully: {}", objectName);

            return FileUploadResponse.builder()
                    .uploadId(saved.getId().toString())
                    .sessionId(sessionId)
                    .objectName(objectName)
                    .fileName(session.getFileName())
                    .fileSize(stat.size())
                    .category(session.getCategory())
                    .uploadUrl(buildFileUrl(objectName))
                    .status("completed")
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error merging chunks for session {}", sessionId, e);
            throw new BusinessException("分片合并失败: " + e.getMessage());
        }
    }

    @Override
    public UploadProgressVO getProgress(String sessionId) {
        UploadSession session = sessionManager.getSession(sessionId);
        if (session == null) {
            throw new BusinessException("上传会话不存在");
        }
        return UploadProgressVO.builder()
                .sessionId(sessionId)
                .progress(session.getProgress())
                .uploadedChunks(session.getUploadedChunks().size())
                .totalChunks(session.getTotalChunks())
                .fileName(session.getFileName())
                .fileSize(session.getFileSize())
                .build();
    }

    @Override
    public void cancelUpload(String sessionId) {
        UploadSession session = sessionManager.getSession(sessionId);
        if (session == null) return;

        for (int i = 0; i < session.getTotalChunks(); i++) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(properties.getBucketName())
                        .object(session.getObjectName() + ".chunk" + i)
                        .build());
            } catch (Exception e) {
                log.warn("Failed to delete chunk {} during cancel: {}", i, e.getMessage());
            }
        }

        sessionManager.removeSession(sessionId);
        log.info("Upload cancelled: {}", sessionId);
    }

    @Override
    public InputStream downloadFile(String objectName) throws Exception {
        String bucketName = properties.getBucketName();
        // 确保存储桶存在
        ensureBucketExists(bucketName);
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    @Override
    public void deleteFile(String objectName) throws Exception {
        String bucketName = properties.getBucketName();
        ensureBucketExists(bucketName);
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
        fileUploadRepository.findByObjectNameAndIsDeletedFalse(objectName)
                .ifPresent(f -> {
                    f.setIsDeleted(true);
                    fileUploadRepository.updateById(f);
                });
    }

    @Override
    public String getPreviewUrl(String objectName) throws Exception {
        String bucketName = properties.getBucketName();
        ensureBucketExists(bucketName);
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(7, TimeUnit.DAYS)
                        .build());
    }

    private String buildFileUrl(String objectName) {
        return properties.getEndpoint() + "/" + properties.getBucketName() + "/" + objectName;
    }

    /**
     * 根据业务类别生成 Bucket 名称
     */
    private String generateBucketName(String category) {
        String baseBucket = properties.getBucketName().toLowerCase();
        if (category != null && !category.isBlank()) {
            String bucketName = baseBucket + "-" + category.toLowerCase();
            log.info("📦 Generated bucket name for category '{}': {}", category, bucketName);
            return bucketName;
        }
        log.info("📦 Using default bucket name: {}", baseBucket);
        return baseBucket;
    }

    /**
     * 确保 Bucket 存在，如不存在则自动创建
     */
    private void ensureBucketExists(String bucketName) {
        try {
            // 检查存储桶是否存在
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                log.warn("⚠️  Bucket '{}' does not exist, creating...", bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("✅ Successfully created MinIO bucket: {}", bucketName);
            } else {
                log.debug("✓ Bucket '{}' already exists", bucketName);
            }
        } catch (Exception e) {
            log.error("❌ Failed to ensure bucket '{}' exists: {}", bucketName, e.getMessage(), e);
            throw new BusinessException("Failed to initialize storage: " + e.getMessage());
        }
    }

    @Override
    public BatchUploadResultVO batchUpload(MultipartFile[] files, String category) {
        log.info("🚀 Starting batch upload: {} files, category: '{}'", files.length, category);
        
        List<FileUploadResponse> successes = new ArrayList<>(files.length);
        List<String> failures = new ArrayList<>(files.length);
        
        // 统一使用默认存储桶，不再按分类分离
        String bucketName = properties.getBucketName();
        
        // 确保存储桶存在
        ensureBucketExists(bucketName);
        log.info("📍 Using bucket: {}", bucketName);
        
        // 获取当前用户 ID
        Long currentUserId = null;
        try {
            currentUserId = SecurityUtils.getCurrentUser().getId();
            log.info("👤 Upload initiated by user ID: {}", currentUserId);
        } catch (Exception e) {
            log.warn("⚠️  Cannot get current user ID for file upload");
        }

        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename() != null
                    ? file.getOriginalFilename() : "unknown";
            try {
                String objectName = UploadSessionManager.generateObjectName(originalName);
                String contentType = file.getContentType() != null
                        ? file.getContentType() : "application/octet-stream";

                log.debug("📤 Uploading file: {} (size: {} bytes, type: {})", originalName, file.getSize(), contentType);

                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(contentType)
                        .build());

                FileUploadPO fileUpload = FileUploadPO.builder()
                        .objectName(objectName)
                        .fileName(originalName)
                        .fileSize(file.getSize())
                        .contentType(contentType)
                        .category(category)
                        .uploadUrl(buildFileUrl(objectName))
                        .uploadedAt(LocalDateTime.now())
                        .uploadedBy(currentUserId)
                        .build();
                fileUploadRepository.insert(fileUpload);
                FileUploadPO saved = fileUpload;

                successes.add(FileUploadResponse.builder()
                        .uploadId(saved.getId().toString())
                        .objectName(objectName)
                        .fileName(originalName)
                        .fileSize(file.getSize())
                        .category(category)
                        .uploadUrl(buildFileUrl(objectName))
                        .status("completed")
                        .build());

                log.info("✅ File uploaded successfully: {} -> bucket: {} | objectName: {}", originalName, bucketName, objectName);
            } catch (Exception e) {
                log.error("❌ File upload failed: {} | Error: {}", originalName, e.getMessage(), e);
                failures.add(originalName + ": " + e.getMessage());
            }
        }

        log.info("📊 Batch upload completed: {} success, {} failures out of {} total", 
                 successes.size(), failures.size(), files.length);
        
        return BatchUploadResultVO.builder()
                .totalFiles(files.length)
                .successCount(successes.size())
                .failureCount(failures.size())
                .successResults(successes)
                .failureMessages(failures)
                .build();
    }
}
