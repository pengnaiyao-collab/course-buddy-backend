package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("file_uploads")
public class FileUploadPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String objectName;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private String category;
    private String uploadUrl;
    private LocalDateTime uploadedAt;
    private Long uploadedBy;
    @Builder.Default
    private Boolean isDeleted = false;
}
