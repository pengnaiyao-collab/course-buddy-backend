package com.coursebuddy.domain.vo;

import com.coursebuddy.domain.auth.Role;
import com.coursebuddy.domain.auth.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseVO {

    private String token;
    private String tokenType;
    private Long userId;
    private String username;
    private String realName;
    private Role role;

    public static AuthResponseVO of(String token, User user) {
        return AuthResponseVO.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .build();
    }
}
