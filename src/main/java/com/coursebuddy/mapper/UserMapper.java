package com.coursebuddy.mapper;

import com.coursebuddy.domain.dto.RegisterDTO;
import com.coursebuddy.domain.po.UserPO;
import com.coursebuddy.domain.vo.LoginResponseVO;
import com.coursebuddy.domain.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserPO registerDtoToPo(RegisterDTO dto) {
        if (dto == null) return null;
        return UserPO.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .isActive(true)
                .isLocked(false)
                .build();
    }

    public UserVO poToVo(UserPO po) {
        if (po == null) return null;
        return UserVO.builder()
                .id(po.getId())
                .username(po.getUsername())
                .email(po.getEmail())
                .realName(po.getRealName())
                .phone(po.getPhone())
                .avatar(po.getAvatar())
                .isActive(po.getIsActive())
                .createdAt(po.getCreatedAt())
                .lastLoginAt(po.getLastLoginAt())
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
