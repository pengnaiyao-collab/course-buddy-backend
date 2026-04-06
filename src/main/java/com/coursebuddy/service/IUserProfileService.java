package com.coursebuddy.service;

import com.coursebuddy.domain.dto.UserProfileDTO;
import com.coursebuddy.domain.vo.UserProfileVO;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 用户资料服务
 */
public interface IUserProfileService {
    UserProfileVO getMyProfile();
    UserProfileVO updateMyProfile(UserProfileDTO dto);
    UserProfileVO updateMyAvatar(MultipartFile file);
    UserProfileVO getProfileById(Long userId);
    Page<UserProfileVO> searchUsers(String keyword, String status, String role, Pageable pageable);
    Page<UserProfileVO> getPendingTeachers(Pageable pageable);
    void approveTeacher(Long userId);
    void rejectTeacher(Long userId);
}
