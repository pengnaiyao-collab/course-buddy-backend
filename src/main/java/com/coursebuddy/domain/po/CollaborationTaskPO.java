package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("collaboration_tasks")
public class CollaborationTaskPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private Long assigneeId;
    private Long creatorId;
    @Builder.Default
    private String status = "TODO";
    @Builder.Default
    private String priority = "MEDIUM";
    private LocalDate dueDate;
    private LocalDateTime completedAt;
    @Builder.Default
    private Integer progress = 0;
    @TableLogic
    private LocalDateTime deletedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
