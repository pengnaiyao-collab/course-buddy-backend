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
@TableName("project_members")
public class ProjectMemberPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long userId;
    @Builder.Default
    private String role = "MEMBER";
    private LocalDateTime joinedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
