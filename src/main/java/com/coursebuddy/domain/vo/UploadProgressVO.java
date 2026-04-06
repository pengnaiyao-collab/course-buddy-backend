package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传进度视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadProgressVO {
    private String sessionId;
    private int progress;
    private int uploadedChunks;
    private int totalChunks;
    private String fileName;
    private long fileSize;
}
