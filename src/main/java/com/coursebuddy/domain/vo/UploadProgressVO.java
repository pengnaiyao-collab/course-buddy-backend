package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
