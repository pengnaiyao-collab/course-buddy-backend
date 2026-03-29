package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAttachmentVO {
    private Long id;
    private Long taskId;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private Long uploadedBy;
    private String uploaderName;
    private LocalDateTime createdAt;
}
