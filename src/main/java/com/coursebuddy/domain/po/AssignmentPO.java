package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 作业持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("assignments")
public class AssignmentPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    @Builder.Default
    private Double maxScore = 100.0;
    private String attachmentUrl;
    @Builder.Default
    private Boolean isPublished = false;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
