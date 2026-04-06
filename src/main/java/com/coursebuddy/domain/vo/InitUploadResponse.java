package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 初始化上传响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitUploadResponse {
    private String sessionId;
    private String objectName;
    private int chunkSize;
    private int totalChunks;
}
