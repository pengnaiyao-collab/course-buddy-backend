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
@TableName("task_attachments")
public class TaskAttachmentPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private Long uploadedBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
