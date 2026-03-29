package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@TableName("course_action_permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseActionPermissionPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String permissionLevel;
    private String actionKey;
    private Boolean allowed;
    private Long updatedBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
