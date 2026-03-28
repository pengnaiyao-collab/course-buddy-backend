package com.coursebuddy.mapper;

import com.coursebuddy.domain.po.UserPO;
import com.coursebuddy.domain.vo.UserProfileVO;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {

    public UserProfileVO poToVo(UserPO po) {
        if (po == null) return null;
        return UserProfileVO.builder()
                .id(po.getId())
                .username(po.getUsername())
                .email(po.getEmail())
                .realName(po.getRealName())
                .phone(po.getPhone())
                .avatar(po.getAvatar())
                .avatarUrl(po.getAvatarUrl())
                .bio(po.getBio())
                .isActive(po.getIsActive())
                .createdAt(po.getCreatedAt())
                .lastLoginAt(po.getLastLoginAt())
                .build();
    }
}
