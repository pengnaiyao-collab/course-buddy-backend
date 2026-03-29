package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 课程权限 PO - 记录用户在特定课程中的权限级别
 * L1: 课程库管理员（最高权限）
 * L2: 核心协作成员
 * L3: 选课班级成员
 * L4: 校内访客成员（只读）
 */
@TableName("course_permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePermissionPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long courseId;

    /**
     * 权限级别: L1, L2, L3, L4
     */
    private String permissionLevel;
    private Long grantedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
