package com.coursebuddy.converter;

import com.coursebuddy.domain.po.UserPO;
import com.coursebuddy.domain.vo.UserProfileVO;
import org.springframework.stereotype.Component;

/**
 * 用户资料转换器
 */
@Component
public class UserProfileConverter {

    public UserProfileVO poToVo(UserPO po) {
        if (po == null) return null;
        return UserProfileVO.builder()
                .id(po.getId())
                .username(po.getUsername())
                .studentNumber(po.getStudentNumber())
                .realName(po.getRealName())
                .school(po.getSchool())
                .avatar(po.getAvatar())
                .avatarUrl(po.getAvatarUrl())
                .bio(po.getBio())
                .role(po.getRole())
                .status(po.getStatus())
                .isActive(po.getIsActive())
                .createdAt(po.getCreatedAt())
                .lastLoginAt(po.getLastLoginAt())
                .build();
    }
}
