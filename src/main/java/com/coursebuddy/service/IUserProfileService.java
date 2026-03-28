package com.coursebuddy.service;

import com.coursebuddy.domain.dto.UserProfileDTO;
import com.coursebuddy.domain.dto.UserSettingsDTO;
import com.coursebuddy.domain.vo.UserProfileVO;
import com.coursebuddy.domain.vo.UserSettingsVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserProfileService {
    UserProfileVO getMyProfile();
    UserProfileVO updateMyProfile(UserProfileDTO dto);
    UserProfileVO getProfileById(Long userId);
    Page<UserProfileVO> searchUsers(String keyword, Pageable pageable);
    UserSettingsVO getMySettings();
    UserSettingsVO updateMySettings(UserSettingsDTO dto);
}
