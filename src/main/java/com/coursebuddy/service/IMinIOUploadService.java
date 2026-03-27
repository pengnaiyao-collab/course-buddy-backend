package com.coursebuddy.service;

import com.coursebuddy.domain.dto.InitUploadRequest;
import com.coursebuddy.domain.vo.ChunkUploadResponse;
import com.coursebuddy.domain.vo.FileUploadResponse;
import com.coursebuddy.domain.vo.InitUploadResponse;
import com.coursebuddy.domain.vo.UploadProgressVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface IMinIOUploadService {

    InitUploadResponse initUpload(InitUploadRequest request);

    ChunkUploadResponse uploadChunk(String sessionId, int chunkIndex, MultipartFile chunk);

    FileUploadResponse mergeChunks(String sessionId, int totalChunks);

    UploadProgressVO getProgress(String sessionId);

    void cancelUpload(String sessionId);

    InputStream downloadFile(String objectName) throws Exception;

    void deleteFile(String objectName) throws Exception;

    String getPreviewUrl(String objectName) throws Exception;
}
