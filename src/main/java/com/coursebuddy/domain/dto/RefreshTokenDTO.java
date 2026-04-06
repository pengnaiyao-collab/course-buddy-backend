package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新令牌传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenDTO {

    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;
}
