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
@TableName("tokens")
public class TokenPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiresAt;
    private LocalDateTime refreshExpiresAt;
    private Boolean isRevoked = false;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
