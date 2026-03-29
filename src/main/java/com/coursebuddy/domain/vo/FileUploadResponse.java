package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String uploadId;
    private String sessionId;
    private String objectName;
    private String fileName;
    private long fileSize;
    private String category;
    private String uploadUrl;
    private String status;
}
