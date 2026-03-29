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
@TableName("user_settings")
public class UserSettingsPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    @Builder.Default
    private Boolean notifyEmail = true;
    @Builder.Default
    private Boolean notifyPush = true;
    @Builder.Default
    private String privacyProfile = "PUBLIC";
    @Builder.Default
    private String language = "zh_CN";
    @Builder.Default
    private String theme = "light";
    @Builder.Default
    private String timezone = "Asia/Shanghai";
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
