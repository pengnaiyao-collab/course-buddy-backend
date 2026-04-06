package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class UserPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String studentNumber;
    private String realName;
    private String school;
    private String avatar;
    private String avatarUrl;
    private String bio;
    private String role;
    private String status;
    @Builder.Default
    private Boolean isActive = true;
    @Builder.Default
    private Boolean isLocked = false;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
}
