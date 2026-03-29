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
@TableName("ai_generated_contents")
public class GeneratedContentPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;

    /** 内容类型: OUTLINE, EXAM_POINTS, QUESTIONS, BREAKDOWN */
    private String contentType;
    private String prompt;
    private String content;
    private Long courseId;
    private String subject;

    /** 状态: PENDING, COMPLETED, FAILED */
    @Builder.Default
    private String status = "PENDING";
    private Integer tokenCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
