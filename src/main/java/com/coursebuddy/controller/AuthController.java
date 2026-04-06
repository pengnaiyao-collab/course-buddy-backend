package com.coursebuddy.controller;

import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.LoginDTO;
import com.coursebuddy.domain.dto.RefreshTokenDTO;
import com.coursebuddy.domain.dto.RegisterDTO;
import com.coursebuddy.domain.po.UserPO;
import com.coursebuddy.domain.vo.LoginResponseVO;
import com.coursebuddy.domain.vo.RefreshTokenResponseVO;
import com.coursebuddy.domain.vo.RegisterResponseVO;
import com.coursebuddy.domain.vo.UserVO;
import com.coursebuddy.mapper.UserMapper;
import com.coursebuddy.service.IAuthService;
import com.coursebuddy.service.ITokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 认证授权控制器 (V1)
 * 负责处理用户注册、登录、Token刷新、密码修改等安全相关的HTTP请求
 */
@Slf4j
@Tag(name = "Authentication V1", description = "V1版本认证与授权接口")
@RestController("authV1Controller")
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final IAuthService authService;
    private final ITokenService tokenService;
    private final UserMapper userMapper;

    /**
     * 用户登录接口
     * 接收用户名和密码，验证通过后返回包含 Access Token 和 Refresh Token 的响应
     *
     * @param loginDto 登录请求参数 (包含用户名、密码)
     * @return 包含双 Token 的成功响应
     */
    @Operation(summary = "用户登录", description = "使用用户名和密码进行登录，返回双Token")
    @PostMapping("/login")
    public ApiResponse<LoginResponseVO> login(@Valid @RequestBody LoginDTO loginDto) {
        log.info("收到用户登录请求: {}", loginDto.getUsername());
        return ApiResponse.success("登录成功", authService.login(loginDto));
    }

    /**
     * 用户注册接口
     * 接收新用户信息，验证防重后将用户存入数据库，并返回初始 Token 或成功提示
     *
     * @param registerDto 注册请求参数 (包含用户名、邮箱、密码等)
     * @return 注册成功的响应信息
     */
    @Operation(summary = "用户注册", description = "注册新用户并分配默认学生角色")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponseVO> register(@Valid @RequestBody RegisterDTO registerDto) {
        log.info("收到用户注册请求: {}", registerDto.getUsername());
        return ApiResponse.success("注册成功", authService.register(registerDto));
    }

    /**
     * 刷新 Token 接口
     * 当 Access Token 过期时，客户端可使用 Refresh Token 换取新的 Access/Refresh Token 对
     *
     * @param refreshTokenDto 包含旧 Refresh Token 的请求体
     * @return 包含新 Token 信息的响应
     */
    @Operation(summary = "刷新 Token", description = "使用有效的 Refresh Token 换取新的双 Token")
    @PostMapping("/refresh-token")
    public ApiResponse<RefreshTokenResponseVO> refreshToken(
            @Valid @RequestBody RefreshTokenDTO refreshTokenDto) {
        return ApiResponse.success("Token 刷新成功", authService.refreshToken(refreshTokenDto));
    }

    /**
     * 用户登出接口
     * 使当前用户的所有活跃 Token（包括刷新 Token）失效，需要鉴权
     *
     * @param authHeader 携带 Bearer Token 的 Authorization 头
     * @return 空数据的成功响应
     */
    @Operation(summary = "用户登出", description = "撤销当前登录用户的所有活跃 Token")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        authService.logout(userId);
        return ApiResponse.success("登出成功", null);
    }

    /**
     * 获取当前登录用户信息接口
     * 解析请求头中的 Token 获取 userId，并返回详细的用户信息 (脱敏)
     *
     * @param authHeader 携带 Bearer Token 的 Authorization 头
     * @return 当前登录用户的详细信息
     */
    @Operation(summary = "获取当前用户信息", description = "根据 Token 解析当前用户信息并返回")
    @GetMapping("/me")
    public ApiResponse<UserVO> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        return ApiResponse.success(authService.getCurrentUser(userId));
    }

    /**
     * 辅助方法：从 Authorization 提取用户 ID
     * 验证 Token 格式是否合法，并解析出内置的 userId
     * 同时检查用户账户是否被锁定
     *
     * @param authHeader HTTP 请求头中的 Authorization 字段
     * @return 解析出的用户 ID
     * @throws BusinessException 当 Token 缺失、格式不合法、过期或用户被锁定时抛出异常
     */
    private Long extractUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("无效的 Authorization 请求头");
        }
        String token = authHeader.substring(7);
        Long userId = tokenService.extractUserIdFromToken(token);
        if (userId == null) {
            throw new BusinessException("无效或已过期的 Token");
        }
        
        // 检查用户账户是否被锁定或禁用
        UserPO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new BusinessException(403, "用户账户已被锁定");
        }
        
        return userId;
    }
}
