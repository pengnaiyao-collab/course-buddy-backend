package com.coursebuddy.service.impl;

import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.po.TokenPO;
import com.coursebuddy.repository.TokenRepository;
import com.coursebuddy.service.ITokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TokenServiceImpl implements ITokenService {

    private final TokenRepository tokenRepository;

    @Value("${jwt.secret:course-buddy-super-secret-key-for-jwt-token-signing-at-least-256-bits}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration:3600}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800}")
    private long refreshTokenExpiration;

    @Override
    public TokenPO generateTokens(Long userId) {
        log.info("Generating tokens for user: {}", userId);

        String accessToken = createAccessToken(userId);
        String refreshToken = UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.now();
        TokenPO token = TokenPO.builder()
                .userId(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresAt(now.plus(accessTokenExpiration, ChronoUnit.SECONDS))
                .refreshExpiresAt(now.plus(refreshTokenExpiration, ChronoUnit.SECONDS))
                .isRevoked(false)
                .build();

        return tokenRepository.save(token);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long extractUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            log.warn("Error extracting user id from token: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public TokenPO refreshAccessToken(String refreshToken) {
        log.info("Refreshing access token");

        TokenPO token = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        if (Boolean.TRUE.equals(token.getIsRevoked())) {
            throw new BusinessException("Refresh token has been revoked");
        }

        if (LocalDateTime.now().isAfter(token.getRefreshExpiresAt())) {
            throw new BusinessException("Refresh token has expired");
        }

        token.setIsRevoked(true);
        tokenRepository.save(token);

        return generateTokens(token.getUserId());
    }

    @Override
    public void revokeToken(String refreshToken) {
        log.info("Revoking token");
        tokenRepository.findByRefreshToken(refreshToken)
                .ifPresent(token -> {
                    token.setIsRevoked(true);
                    tokenRepository.save(token);
                });
    }

    @Override
    public void revokeAllUserTokens(Long userId) {
        log.info("Revoking all tokens for user: {}", userId);
        tokenRepository.findByUserId(userId)
                .forEach(token -> {
                    token.setIsRevoked(true);
                    tokenRepository.save(token);
                });
    }

    private String createAccessToken(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
