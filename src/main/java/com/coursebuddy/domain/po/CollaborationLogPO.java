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
@TableName("collaboration_logs")
public class CollaborationLogPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long userId;
    private String actionType;
    private String entityType;
    private Long entityId;
    private String changeData;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
