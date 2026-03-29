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
@TableName("web_imports")
public class WebImportPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String url;
    private String title;
    private String content;
    private String htmlContent;
    @Builder.Default
    private String status = "PENDING";
    private String errorMessage;
    private Long knowledgeItemId;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
