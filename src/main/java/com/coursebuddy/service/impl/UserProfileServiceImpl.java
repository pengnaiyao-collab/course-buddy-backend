package com.coursebuddy.service.impl;

import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.domain.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.UserProfileDTO;
import com.coursebuddy.domain.po.UserPO;
import com.coursebuddy.domain.vo.UserProfileVO;
import com.coursebuddy.converter.UserProfileConverter;
import com.coursebuddy.mapper.UserMapper;
import com.coursebuddy.service.IUserProfileService;
import com.coursebuddy.service.IMinIOUploadService;
import com.coursebuddy.domain.vo.BatchUploadResultVO;
import com.coursebuddy.domain.vo.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 用户资料服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements IUserProfileService {

    private final UserMapper userRepository;
    private final UserProfileConverter userProfileMapper;
    private final IMinIOUploadService uploadService;

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
        if (dto.getSchool() != null) po.setSchool(dto.getSchool());
        if (dto.getBio() != null) po.setBio(dto.getBio());
        userRepository.updateById(po);
        return userProfileMapper.poToVo(po);
    }

    @Override
    @Transactional
    public UserProfileVO updateMyAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的头像");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new BusinessException(400, "仅支持图片格式头像");
        }
        long maxSize = 5 * 1024 * 1024L;
        if (file.getSize() > maxSize) {
            throw new BusinessException(400, "头像大小不能超过 5MB");
        }

        User currentUser = SecurityUtils.getCurrentUser();
        UserPO po = userRepository.selectById(currentUser.getId());
        if (po == null) {
            throw new BusinessException(404, "User not found");
        }

        String previousObjectName = extractObjectName(po.getAvatarUrl());
        if (previousObjectName == null) {
            previousObjectName = extractObjectName(po.getAvatar());
        }

        BatchUploadResultVO result = uploadService.batchUpload(new MultipartFile[]{file}, "avatar");
        if (result.getSuccessResults() == null || result.getSuccessResults().isEmpty()) {
            String message = (result.getFailureMessages() == null || result.getFailureMessages().isEmpty())
                    ? "头像上传失败"
                    : result.getFailureMessages().get(0);
            throw new BusinessException(500, message);
        }

        FileUploadResponse upload = result.getSuccessResults().get(0);
        String encodedObjectName = URLEncoder.encode(upload.getObjectName(), StandardCharsets.UTF_8)
            .replace("+", "%20");
        String previewUrl = "/api/v1/files/avatar?objectName=" + encodedObjectName + "&preview=true";
        po.setAvatar(previewUrl);
        po.setAvatarUrl(previewUrl);
        userRepository.updateById(po);

        if (previousObjectName != null && !previousObjectName.equals(upload.getObjectName())) {
            try {
                uploadService.deleteFile(previousObjectName);
            } catch (Exception e) {
                log.warn("Failed to delete previous avatar: {}", e.getMessage());
            }
        }
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
    public Page<UserProfileVO> searchUsers(String keyword, String status, String role, Pageable pageable) {
        QueryWrapper<UserPO> query = new QueryWrapper<>();

        if (keyword != null && !keyword.isBlank()) {
            query.and(wrapper -> wrapper
                    .like("username", keyword)
                    .or()
                    .like("real_name", keyword));
        }

        if (status != null && !status.isBlank()) {
            List<String> statuses = Arrays.stream(status.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .map(String::toUpperCase)
                    .toList();
            if (!statuses.isEmpty()) {
                query.in("status", statuses);
            }
        }

        if (role != null && !role.isBlank()) {
            String normalizedRole = role.trim().toUpperCase();
            if (normalizedRole.startsWith("ROLE_")) {
                normalizedRole = normalizedRole.substring(5);
            }
            query.eq("role", normalizedRole);
        }

        IPage<UserPO> poPage = userRepository.selectPage(
                MybatisPlusPageUtils.toMpPage(pageable), query);
        return MybatisPlusPageUtils.toSpringPage(poPage, pageable)
                .map(userProfileMapper::poToVo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileVO> getPendingTeachers(Pageable pageable) {
        IPage<UserPO> poPage = userRepository.findByRoleAndStatus(
                MybatisPlusPageUtils.toMpPage(pageable), "TEACHER", "PENDING");
        return MybatisPlusPageUtils.toSpringPage(poPage, pageable).map(userProfileMapper::poToVo);
    }

    @Override
    @Transactional
    public void approveTeacher(Long userId) {
        UserPO user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "找不到该用户");
        }
        
        // 确保只有处于待审核状态的教师才能被审核通过
        if (!"PENDING".equals(user.getStatus())) {
            throw new BusinessException(400, "该用户状态已变更或无需审核: " + user.getStatus());
        }
        
        // 设置为 ACTIVE 并确保角色为 TEACHER（防止在 PENDING 状态下角色被误改）
        user.setStatus("ACTIVE");
        user.setRole("TEACHER"); 
        
        userRepository.updateById(user);
        log.info("教师账号审核通过: userId={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    @Transactional
    public void rejectTeacher(Long userId) {
        UserPO user = userRepository.selectById(userId);
        if (user == null || !"PENDING".equals(user.getStatus())) {
            throw new BusinessException(400, "用户不存在或状态不是待审核");
        }
        user.setStatus("REJECTED");
        userRepository.updateById(user);
    }

    private String extractObjectName(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return null;
        }
        int start = avatarUrl.indexOf("objectName=");
        if (start < 0) {
            return null;
        }
        String value = avatarUrl.substring(start + "objectName=".length());
        int amp = value.indexOf('&');
        if (amp >= 0) {
            value = value.substring(0, amp);
        }
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
