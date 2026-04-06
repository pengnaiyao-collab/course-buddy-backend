package com.coursebuddy.service;

import com.coursebuddy.domain.dto.InitUploadRequest;
import com.coursebuddy.domain.vo.BatchUploadResultVO;
import com.coursebuddy.domain.vo.ChunkUploadResponse;
import com.coursebuddy.domain.vo.FileUploadResponse;
import com.coursebuddy.domain.vo.InitUploadResponse;
import com.coursebuddy.domain.vo.UploadProgressVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 上传服务
 */
public interface IMinIOUploadService {

    InitUploadResponse initUpload(InitUploadRequest request);

    ChunkUploadResponse uploadChunk(String sessionId, int chunkIndex, MultipartFile chunk);

    FileUploadResponse mergeChunks(String sessionId, int totalChunks);

    UploadProgressVO getProgress(String sessionId);

    void cancelUpload(String sessionId);

    InputStream downloadFile(String objectName) throws Exception;

    void deleteFile(String objectName) throws Exception;

    String getPreviewUrl(String objectName) throws Exception;

    /**
     * 批量上传多个文件，每个文件作为独立对象存储。
     * 返回成功与失败的汇总结果。
     */
    BatchUploadResultVO batchUpload(MultipartFile[] files, String category);
}
