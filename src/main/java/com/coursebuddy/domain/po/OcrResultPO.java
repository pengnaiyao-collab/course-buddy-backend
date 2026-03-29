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
@TableName("ocr_results")
public class OcrResultPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fileUploadId;
    private String objectName;
    private String extractedText;
    private Double confidence;
    @Builder.Default
    private String language = "chi_sim+eng";
    @Builder.Default
    private String status = "PENDING";
    private String errorMessage;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
