package com.coursebuddy.service.impl;

import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.po.UserPO;
import com.coursebuddy.mapper.UserMapper;
import com.coursebuddy.domain.po.TokenPO;
import com.coursebuddy.mapper.TokenMapper;
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

/**
 * Token 管理服务实现类
 * 负责 JWT Access Token 和 Refresh Token 的生成、校验、解析、刷新与撤销等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TokenServiceImpl implements ITokenService {

    private final TokenMapper tokenRepository;
    private final UserMapper userRepository;

    @Value("${jwt.secret:course-buddy-super-secret-key-for-jwt-token-signing-at-least-256-bits}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration:3600}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800}")
    private long refreshTokenExpiration;

    /**
     * 为指定用户生成新的双 Token
     * 1. 生成基于 JWT 的 Access Token
     * 2. 生成基于 UUID 的 Refresh Token
     * 3. 将 Token 记录存入数据库，供后续验证和撤销使用
     *
     * @param userId 目标用户 ID
     * @return 包含新生成 Token 信息的持久化对象
     */
    @Override
    public TokenPO generateTokens(Long userId) {
        log.info("为用户生成 Token: {}", userId);

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

        tokenRepository.insert(token);
        return token;
    }

    /**
     * 校验 Access Token 的有效性
     * 验证 JWT 的签名及是否过期
     *
     * @param token 待校验的 JWT 字符串
     * @return true 若 Token 合法且未过期，否则 false
     */
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
            log.warn("无效的 Token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从 Access Token 中解析用户 ID
     *
     * @param token JWT 字符串
     * @return 提取到的用户 ID；若解析失败或 Token 异常则返回 null
     */
    @Override
    @Transactional(readOnly = true)
    public Long extractUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Object userIdClaim = claims.get("userId");
            if (userIdClaim != null) {
                return Long.valueOf(String.valueOf(userIdClaim));
            }
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            log.warn("从 Token 提取用户 ID 时出错: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 使用 Refresh Token 换取新的双 Token
     * 1. 验证 Refresh Token 的有效性（是否存在、是否被撤销、是否过期）
     * 2. 将旧 Refresh Token 标记为已撤销（防止重放）
     * 3. 生成新的 Token 并返回
     *
     * @param refreshToken 客户端提供的旧 Refresh Token
     * @return 新生成的 Token 对象
     * @throws BusinessException 当 Refresh Token 无效或过期时抛出
     */
    @Override
    public TokenPO refreshAccessToken(String refreshToken) {
        log.info("刷新 Access Token");

        TokenPO token = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException("无效的 Refresh Token"));

        if (Boolean.TRUE.equals(token.getIsRevoked())) {
            throw new BusinessException("该 Refresh Token 已被撤销");
        }

        if (LocalDateTime.now().isAfter(token.getRefreshExpiresAt())) {
            throw new BusinessException("该 Refresh Token 已过期");
        }

        token.setIsRevoked(true);
        tokenRepository.updateById(token);

        return generateTokens(token.getUserId());
    }

    /**
     * 撤销指定的 Refresh Token
     * 将对应的数据库记录的 isRevoked 字段置为 true
     *
     * @param refreshToken 待撤销的 Refresh Token
     */
    @Override
    public void revokeToken(String refreshToken) {
        log.info("撤销 Token");
        tokenRepository.findByRefreshToken(refreshToken)
                .ifPresent(token -> {
                    token.setIsRevoked(true);
                    tokenRepository.updateById(token);
                });
    }

    /**
     * 撤销指定用户的所有活跃 Token
     * 通常在用户登出或修改密码后调用
     *
     * @param userId 目标用户 ID
     */
    @Override
    public void revokeAllUserTokens(Long userId) {
        log.info("撤销该用户的所有 Token: {}", userId);
        tokenRepository.findByUserId(userId)
                .forEach(token -> {
                    token.setIsRevoked(true);
                    tokenRepository.updateById(token);
                });
    }

    /**
     * 内部方法：生成带有签名和过期时间的 JWT Access Token
     *
     * @param userId 要写入 Claim 的用户 ID
     * @return 签名的 JWT 字符串
     */
    private String createAccessToken(Long userId) {
        UserPO user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "找不到该用户");
        }
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
