package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新令牌响应视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponseVO {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType = "Bearer";
}
