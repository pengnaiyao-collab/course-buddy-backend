package com.coursebuddy.service;

import com.coursebuddy.domain.dto.LoginDTO;
import com.coursebuddy.domain.dto.RefreshTokenDTO;
import com.coursebuddy.domain.dto.RegisterDTO;
import com.coursebuddy.domain.po.UserPO;
import com.coursebuddy.domain.vo.LoginResponseVO;
import com.coursebuddy.domain.vo.RefreshTokenResponseVO;
import com.coursebuddy.domain.vo.RegisterResponseVO;
import com.coursebuddy.domain.vo.UserVO;

/**
 * 认证服务
 */
public interface IAuthService {

    LoginResponseVO login(LoginDTO loginDto);

    RegisterResponseVO register(RegisterDTO registerDto);

    RefreshTokenResponseVO refreshToken(RefreshTokenDTO refreshTokenDto);

    void logout(Long userId);


    UserVO getCurrentUser(Long userId);

    UserPO getUserByUsername(String username);

    UserPO getUserByEmail(String email);

    boolean usernameExists(String username);

    boolean emailExists(String email);
}
