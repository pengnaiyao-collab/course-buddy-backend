package com.coursebuddy.controller;

import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.ChangePasswordDTO;
import com.coursebuddy.domain.dto.LoginDTO;
import com.coursebuddy.domain.dto.RefreshTokenDTO;
import com.coursebuddy.domain.dto.RegisterDTO;
import com.coursebuddy.domain.vo.LoginResponseVO;
import com.coursebuddy.domain.vo.RefreshTokenResponseVO;
import com.coursebuddy.domain.vo.RegisterResponseVO;
import com.coursebuddy.domain.vo.UserVO;
import com.coursebuddy.service.IAuthService;
import com.coursebuddy.service.ITokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("authV1Controller")
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final IAuthService authService;
    private final ITokenService tokenService;

    @PostMapping("/login")
    public ApiResponse<LoginResponseVO> login(@Valid @RequestBody LoginDTO loginDto) {
        log.info("Login request for user: {}", loginDto.getUsername());
        return ApiResponse.success("Login successful", authService.login(loginDto));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponseVO> register(@Valid @RequestBody RegisterDTO registerDto) {
        log.info("Register request for user: {}", registerDto.getUsername());
        return ApiResponse.success("Registration successful", authService.register(registerDto));
    }

    @PostMapping("/refresh-token")
    public ApiResponse<RefreshTokenResponseVO> refreshToken(
            @Valid @RequestBody RefreshTokenDTO refreshTokenDto) {
        return ApiResponse.success("Token refreshed", authService.refreshToken(refreshTokenDto));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        authService.logout(userId);
        return ApiResponse.success("Logged out successfully", null);
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChangePasswordDTO changePasswordDto) {
        Long userId = extractUserIdFromHeader(authHeader);
        authService.changePassword(userId, changePasswordDto);
        return ApiResponse.success("Password changed successfully", null);
    }

    @GetMapping("/me")
    public ApiResponse<UserVO> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        return ApiResponse.success(authService.getCurrentUser(userId));
    }

    private Long extractUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("Invalid authorization header");
        }
        String token = authHeader.substring(7);
        Long userId = tokenService.extractUserIdFromToken(token);
        if (userId == null) {
            throw new BusinessException("Invalid or expired token");
        }
        return userId;
    }
}
