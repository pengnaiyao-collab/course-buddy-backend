package com.coursebuddy.converter;

import com.coursebuddy.domain.dto.RegisterDTO;
import com.coursebuddy.domain.po.UserPO;
import com.coursebuddy.domain.vo.LoginResponseVO;
import com.coursebuddy.domain.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 用户转换器
 */
@Component
@RequiredArgsConstructor
public class UserConverter {

    private final PasswordEncoder passwordEncoder;

    public UserPO registerDtoToPo(RegisterDTO dto) {
        if (dto == null) return null;
        return UserPO.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .studentNumber(dto.getStudentNumber())
                .realName(dto.getRealName())
                .role(dto.getRole())
                .isActive(true)
                .isLocked(false)
                .build();
    }

    public UserVO poToVo(UserPO po) {
        if (po == null) return null;
        return UserVO.builder()
                .id(po.getId())
                .username(po.getUsername())
                .studentNumber(po.getStudentNumber())
                .realName(po.getRealName())
                .school(po.getSchool())
                .avatar(po.getAvatar())
                .avatarUrl(po.getAvatarUrl())
                .role(po.getRole())
                .status(po.getStatus())
                .isActive(po.getIsActive())
                .createdAt(po.getCreatedAt())
                .lastLoginAt(po.getLastLoginAt())
                .lastLoginIp(po.getLastLoginIp())
                .build();
    }

    public LoginResponseVO poAndTokenToLoginResponse(UserPO po, String accessToken,
                                                      String refreshToken, Long expiresIn) {
        if (po == null) return null;
        return LoginResponseVO.builder()
                .user(poToVo(po))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .tokenType("Bearer")
                .build();
    }
}
