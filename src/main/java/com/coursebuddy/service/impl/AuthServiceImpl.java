package com.coursebuddy.service.impl;

import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.common.exception.ResourceNotFoundException;
import com.coursebuddy.domain.dto.ChangePasswordDTO;
import com.coursebuddy.domain.dto.LoginDTO;
import com.coursebuddy.domain.dto.RefreshTokenDTO;
import com.coursebuddy.domain.dto.RegisterDTO;
import com.coursebuddy.domain.po.TokenPO;
import com.coursebuddy.domain.po.UserPO;
import com.coursebuddy.domain.vo.LoginResponseVO;
import com.coursebuddy.domain.vo.RefreshTokenResponseVO;
import com.coursebuddy.domain.vo.RegisterResponseVO;
import com.coursebuddy.domain.vo.UserVO;
import com.coursebuddy.converter.UserConverter;
import com.coursebuddy.mapper.UserMapper;
import com.coursebuddy.service.IAuthService;
import com.coursebuddy.service.ITokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements IAuthService {

    private final UserMapper userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserConverter userMapper;
    private final ITokenService tokenService;

    @Override
    public LoginResponseVO login(LoginDTO loginDto) {
        log.info("User login attempt: {}", loginDto.getUsername());

        UserPO user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new BusinessException("Username or password incorrect"));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new BusinessException("Username or password incorrect");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BusinessException("User account is inactive");
        }
        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new BusinessException("User account is locked");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.updateById(user);

        TokenPO token = tokenService.generateTokens(user.getId());
        long expiresIn = ChronoUnit.SECONDS.between(LocalDateTime.now(), token.getExpiresAt());

        return userMapper.poAndTokenToLoginResponse(
                user, token.getAccessToken(), token.getRefreshToken(), expiresIn);
    }

    @Override
    public RegisterResponseVO register(RegisterDTO registerDto) {
        log.info("User registration attempt: {}", registerDto.getUsername());

        if (usernameExists(registerDto.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (emailExists(registerDto.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        UserPO user = userMapper.registerDtoToPo(registerDto);
        userRepository.insert(user);
        UserPO savedUser = user;
        log.info("User registered successfully: {}", savedUser.getId());

        return RegisterResponseVO.builder()
                .user(userMapper.poToVo(savedUser))
                .message("Registration successful, please login")
                .build();
    }

    @Override
    public RefreshTokenResponseVO refreshToken(RefreshTokenDTO refreshTokenDto) {
        log.info("Token refresh attempt");

        TokenPO newToken = tokenService.refreshAccessToken(refreshTokenDto.getRefreshToken());
        long expiresIn = ChronoUnit.SECONDS.between(LocalDateTime.now(), newToken.getExpiresAt());

        return RefreshTokenResponseVO.builder()
                .accessToken(newToken.getAccessToken())
                .refreshToken(newToken.getRefreshToken())
                .expiresIn(expiresIn)
                .tokenType("Bearer")
                .build();
    }

    @Override
    public void logout(Long userId) {
        log.info("User logout: {}", userId);
        tokenService.revokeAllUserTokens(userId);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO changePasswordDto) {
        log.info("User change password: {}", userId);

        UserPO user = userRepository.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.updateById(user);

        tokenService.revokeAllUserTokens(userId);
        log.info("Password changed successfully, all tokens revoked: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserVO getCurrentUser(Long userId) {
        UserPO user = userRepository.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        return userMapper.poToVo(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPO getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserPO getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
