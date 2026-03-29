package com.coursebuddy.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadSession implements Serializable {

    private String sessionId;
    private String fileName;
    private long fileSize;
    private String category;
    private int totalChunks;
    private String objectName;
    private int progress;
    private LocalDateTime createdAt;

    @Builder.Default
    private ConcurrentHashMap<Integer, Boolean> uploadedChunks = new ConcurrentHashMap<>();
}
