package com.coursebuddy.service.impl;

import com.coursebuddy.auth.Role;
import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.CoursePermissionDTO;
import com.coursebuddy.domain.po.CoursePermissionPO;
import com.coursebuddy.domain.vo.CoursePermissionVO;
import com.coursebuddy.repository.CoursePermissionRepository;
import com.coursebuddy.service.ICoursePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoursePermissionServiceImpl implements ICoursePermissionService {

    private final CoursePermissionRepository permissionRepository;

    private static final Map<String, String> LEVEL_NAMES = Map.of(
            "L1", "课程库管理员",
            "L2", "核心协作成员",
            "L3", "选课班级成员",
            "L4", "校内访客成员"
    );

    /** 权限级别数值（数字越小，权限越高） */
    private int levelValue(String level) {
        return switch (level.toUpperCase()) {
            case "L1" -> 1;
            case "L2" -> 2;
            case "L3" -> 3;
            case "L4" -> 4;
            default -> 99;
        };
    }

    @Override
    @Transactional
    public CoursePermissionVO grantPermission(CoursePermissionDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        requireAdminOrL1(currentUser, dto.getCourseId());

        if (permissionRepository.existsByUserIdAndCourseId(dto.getUserId(), dto.getCourseId())) {
            throw new BusinessException(409, "用户已拥有该课程的权限，请使用更新接口");
        }

        CoursePermissionPO po = CoursePermissionPO.builder()
                .userId(dto.getUserId())
                .courseId(dto.getCourseId())
                .permissionLevel(dto.getPermissionLevel().toUpperCase())
                .grantedBy(currentUser.getId())
                .build();

        log.info("Granting {} permission to user {} for course {} by user {}",
                dto.getPermissionLevel(), dto.getUserId(), dto.getCourseId(), currentUser.getId());
        return toVO(permissionRepository.save(po));
    }

    @Override
    @Transactional
    public CoursePermissionVO updatePermission(CoursePermissionDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        requireAdminOrL1(currentUser, dto.getCourseId());

        CoursePermissionPO po = permissionRepository
                .findByUserIdAndCourseId(dto.getUserId(), dto.getCourseId())
                .orElseThrow(() -> new BusinessException(404, "用户在该课程中没有权限记录"));

        po.setPermissionLevel(dto.getPermissionLevel().toUpperCase());
        po.setGrantedBy(currentUser.getId());
        return toVO(permissionRepository.save(po));
    }

    @Override
    @Transactional
    public void revokePermission(Long userId, Long courseId) {
        User currentUser = SecurityUtils.getCurrentUser();
        requireAdminOrL1(currentUser, courseId);

        if (!permissionRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new BusinessException(404, "用户在该课程中没有权限记录");
        }
        permissionRepository.deleteByUserIdAndCourseId(userId, courseId);
        log.info("Revoked permission for user {} in course {} by {}", userId, courseId, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoursePermissionVO> getCourseMembers(Long courseId) {
        return permissionRepository.findByCourseId(courseId).stream()
                .map(this::toVO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoursePermissionVO> getUserPermissions(Long userId) {
        return permissionRepository.findByUserId(userId).stream()
                .map(this::toVO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoursePermissionVO> getCourseAdmins(Long courseId) {
        return permissionRepository.findByCourseIdAndPermissionLevel(courseId, "L1").stream()
                .map(this::toVO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long courseId, String minLevel) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() == Role.ADMIN) return true;
        return permissionRepository.findByUserIdAndCourseId(currentUser.getId(), courseId)
                .map(po -> levelValue(po.getPermissionLevel()) <= levelValue(minLevel))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public String getPermissionLevel(Long courseId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() == Role.ADMIN) return "L1";
        return permissionRepository.findByUserIdAndCourseId(currentUser.getId(), courseId)
                .map(CoursePermissionPO::getPermissionLevel)
                .orElse(null);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private void requireAdminOrL1(User user, Long courseId) {
        if (user.getRole() == Role.ADMIN) return;
        boolean isL1 = permissionRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .map(p -> "L1".equals(p.getPermissionLevel()))
                .orElse(false);
        if (!isL1) {
            throw new BusinessException(403, "只有课程管理员（L1）或系统管理员才能管理课程权限");
        }
    }

    private CoursePermissionVO toVO(CoursePermissionPO po) {
        return CoursePermissionVO.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .courseId(po.getCourseId())
                .permissionLevel(po.getPermissionLevel())
                .permissionLevelName(LEVEL_NAMES.getOrDefault(po.getPermissionLevel(), po.getPermissionLevel()))
                .grantedBy(po.getGrantedBy())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }
}
