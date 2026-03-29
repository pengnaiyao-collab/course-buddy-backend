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
@TableName("collaboration_projects")
public class CollaborationProjectPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String name;
    private String description;
    private Long ownerId;
    @Builder.Default
    private String status = "ACTIVE";
    private String coverUrl;
    @Builder.Default
    private Boolean isPublic = false;
    @TableLogic
    private LocalDateTime deletedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
