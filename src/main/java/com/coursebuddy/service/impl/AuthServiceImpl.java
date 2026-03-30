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

/**
 * 认证与授权服务实现类
 * 负责处理登录校验、注册落库、Token刷新、修改密码等核心认证业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements IAuthService {

    private final UserMapper userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserConverter userMapper;
    private final ITokenService tokenService;

    /**
     * 处理用户登录逻辑
     * 1. 根据用户名查询用户是否存在
     * 2. 校验密码是否正确
     * 3. 检查用户账户状态（是否激活、是否被锁定）
     * 4. 更新最后登录时间
     * 5. 生成新的双 Token (Access & Refresh) 并返回
     *
     * @param loginDto 包含用户名和密码的 DTO
     * @return 登录响应，包含脱敏用户信息和 Token
     * @throws BusinessException 校验失败或账户异常时抛出
     */
    @Override
    public LoginResponseVO login(LoginDTO loginDto) {
        log.info("用户尝试登录: {}", loginDto.getUsername());

        UserPO user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BusinessException("该用户账户未激活");
        }
        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new BusinessException("该用户账户已被锁定");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.updateById(user);

        TokenPO token = tokenService.generateTokens(user.getId());
        long expiresIn = ChronoUnit.SECONDS.between(LocalDateTime.now(), token.getExpiresAt());

        return userMapper.poAndTokenToLoginResponse(
                user, token.getAccessToken(), token.getRefreshToken(), expiresIn);
    }

    /**
     * 处理用户注册逻辑
     * 1. 校验用户名和邮箱的唯一性
     * 2. 将 DTO 转换为 PO（内部应包含密码加密）
     * 3. 将新用户插入数据库
     *
     * @param registerDto 包含注册信息的 DTO
     * @return 注册响应，包含新创建的用户信息及提示
     * @throws BusinessException 用户名或邮箱已存在时抛出
     */
    @Override
    public RegisterResponseVO register(RegisterDTO registerDto) {
        log.info("用户尝试注册: {}", registerDto.getUsername());

        if (usernameExists(registerDto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (emailExists(registerDto.getEmail())) {
            throw new BusinessException("该邮箱已被注册");
        }

        UserPO user = userMapper.registerDtoToPo(registerDto);
        userRepository.insert(user);
        UserPO savedUser = user;
        log.info("用户注册成功: {}", savedUser.getId());

        return RegisterResponseVO.builder()
                .user(userMapper.poToVo(savedUser))
                .message("注册成功，请登录")
                .build();
    }

    /**
     * 处理 Token 刷新逻辑
     * 通过有效的 Refresh Token 生成新的 Access Token 和 Refresh Token
     *
     * @param refreshTokenDto 包含客户端当前持有的 Refresh Token
     * @return 包含新 Token 及过期时间的响应
     */
    @Override
    public RefreshTokenResponseVO refreshToken(RefreshTokenDTO refreshTokenDto) {
        log.info("尝试刷新 Token");

        TokenPO newToken = tokenService.refreshAccessToken(refreshTokenDto.getRefreshToken());
        long expiresIn = ChronoUnit.SECONDS.between(LocalDateTime.now(), newToken.getExpiresAt());

        return RefreshTokenResponseVO.builder()
                .accessToken(newToken.getAccessToken())
                .refreshToken(newToken.getRefreshToken())
                .expiresIn(expiresIn)
                .tokenType("Bearer")
                .build();
    }

    /**
     * 处理用户登出逻辑
     * 撤销该用户下所有的相关 Token，使其失效
     *
     * @param userId 待登出的用户 ID
     */
    @Override
    public void logout(Long userId) {
        log.info("用户登出: {}", userId);
        tokenService.revokeAllUserTokens(userId);
    }

    /**
     * 处理修改密码逻辑
     * 1. 验证用户是否存在
     * 2. 校验旧密码是否匹配
     * 3. 将新密码加密后更新入库
     * 4. 撤销用户所有历史 Token，强制重新登录
     *
     * @param userId 当前用户 ID
     * @param changePasswordDto 包含旧密码和新密码的 DTO
     * @throws ResourceNotFoundException 用户不存在时抛出
     * @throws BusinessException 旧密码错误时抛出
     */
    @Override
    public void changePassword(Long userId, ChangePasswordDTO changePasswordDto) {
        log.info("用户修改密码: {}", userId);

        UserPO user = userRepository.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("找不到该用户");
        }

        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.updateById(user);

        tokenService.revokeAllUserTokens(userId);
        log.info("密码修改成功，已撤销该用户所有活跃 Token: {}", userId);
    }

    /**
     * 获取当前登录用户信息
     *
     * @param userId 当前用户 ID
     * @return 用户 VO（包含脱敏数据）
     * @throws ResourceNotFoundException 用户不存在时抛出
     */
    @Override
    @Transactional(readOnly = true)
    public UserVO getCurrentUser(Long userId) {
        UserPO user = userRepository.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("找不到该用户");
        }
        return userMapper.poToVo(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPO getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("找不到该用户"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserPO getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("找不到该用户"));
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
