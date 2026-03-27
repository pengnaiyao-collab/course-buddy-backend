package com.coursebuddy.service;

import com.coursebuddy.domain.po.TokenPO;

public interface ITokenService {

    TokenPO generateTokens(Long userId);

    boolean validateAccessToken(String token);

    Long extractUserIdFromToken(String token);

    TokenPO refreshAccessToken(String refreshToken);

    void revokeToken(String refreshToken);

    void revokeAllUserTokens(Long userId);
}
