package com.coursebuddy.service.impl;

import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.UserProfileDTO;
import com.coursebuddy.domain.dto.UserSettingsDTO;
import com.coursebuddy.domain.po.UserPO;
import com.coursebuddy.domain.po.UserSettingsPO;
import com.coursebuddy.domain.vo.UserProfileVO;
import com.coursebuddy.domain.vo.UserSettingsVO;
import com.coursebuddy.converter.UserProfileConverter;
import com.coursebuddy.mapper.UserMapper;
import com.coursebuddy.mapper.UserSettingsMapper;
import com.coursebuddy.service.IUserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements IUserProfileService {

    private final UserMapper userRepository;
    private final UserSettingsMapper userSettingsRepository;
    private final UserProfileConverter userProfileMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileVO getMyProfile() {
        User currentUser = SecurityUtils.getCurrentUser();
        UserPO po = userRepository.selectById(currentUser.getId());
        if (po == null) {
            throw new BusinessException(404, "User not found");
        }
        return userProfileMapper.poToVo(po);
    }

    @Override
    @Transactional
    public UserProfileVO updateMyProfile(UserProfileDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        UserPO po = userRepository.selectById(currentUser.getId());
        if (po == null) {
            throw new BusinessException(404, "User not found");
        }
        if (dto.getRealName() != null) po.setRealName(dto.getRealName());
        if (dto.getPhone() != null) po.setPhone(dto.getPhone());
        if (dto.getBio() != null) po.setBio(dto.getBio());
        userRepository.updateById(po);
        return userProfileMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileVO getProfileById(Long userId) {
        UserPO po = userRepository.selectById(userId);
        if (po == null) {
            throw new BusinessException(404, "User not found");
        }
        return userProfileMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileVO> searchUsers(String keyword, Pageable pageable) {
        IPage<UserPO> poPage = userRepository.searchByKeyword(
                MybatisPlusPageUtils.toMpPage(pageable), keyword);
        return MybatisPlusPageUtils.toSpringPage(poPage, pageable).map(userProfileMapper::poToVo);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSettingsVO getMySettings() {
        User currentUser = SecurityUtils.getCurrentUser();
        UserSettingsPO settings = userSettingsRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> UserSettingsPO.builder().userId(currentUser.getId()).build());
        return toSettingsVO(settings);
    }

    @Override
    @Transactional
    public UserSettingsVO updateMySettings(UserSettingsDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        UserSettingsPO settings = userSettingsRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> UserSettingsPO.builder().userId(currentUser.getId()).build());
        if (dto.getNotifyEmail() != null) settings.setNotifyEmail(dto.getNotifyEmail());
        if (dto.getNotifyPush() != null) settings.setNotifyPush(dto.getNotifyPush());
        if (dto.getPrivacyProfile() != null) settings.setPrivacyProfile(dto.getPrivacyProfile());
        if (dto.getLanguage() != null) settings.setLanguage(dto.getLanguage());
        if (dto.getTheme() != null) settings.setTheme(dto.getTheme());
        if (dto.getTimezone() != null) settings.setTimezone(dto.getTimezone());
        if (settings.getId() == null) {
            userSettingsRepository.insert(settings);
        } else {
            userSettingsRepository.updateById(settings);
        }
        return toSettingsVO(settings);
    }

    private UserSettingsVO toSettingsVO(UserSettingsPO po) {
        return UserSettingsVO.builder()
                .userId(po.getUserId())
                .notifyEmail(po.getNotifyEmail())
                .notifyPush(po.getNotifyPush())
                .privacyProfile(po.getPrivacyProfile())
                .language(po.getLanguage())
                .theme(po.getTheme())
                .timezone(po.getTimezone())
                .build();
    }
}
