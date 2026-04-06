package com.coursebuddy.converter;

import com.coursebuddy.domain.po.TokenPO;
import com.coursebuddy.domain.vo.RefreshTokenResponseVO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 令牌转换器
 */
@Component
public class TokenConverter {

    public RefreshTokenResponseVO poToRefreshResponse(TokenPO po) {
        if (po == null) return null;
        long expiresIn = ChronoUnit.SECONDS.between(LocalDateTime.now(), po.getExpiresAt());
        return RefreshTokenResponseVO.builder()
                .accessToken(po.getAccessToken())
                .refreshToken(po.getRefreshToken())
                .expiresIn(Math.max(expiresIn, 0))
                .tokenType("Bearer")
                .build();
    }
}
