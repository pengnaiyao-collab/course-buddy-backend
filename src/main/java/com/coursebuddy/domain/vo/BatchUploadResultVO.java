package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量上传视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUploadResultVO {
    private int totalFiles;
    private int successCount;
    private int failureCount;
    private List<FileUploadResponse> successResults;
    private List<String> failureMessages;
}
