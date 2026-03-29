package com.coursebuddy.service.impl;

import com.coursebuddy.auth.Role;
import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.CourseActionPermissionUpdateDTO;
import com.coursebuddy.domain.dto.CoursePermissionDTO;
import com.coursebuddy.domain.po.CourseActionPermissionPO;
import com.coursebuddy.domain.po.CourseAdminVotePO;
import com.coursebuddy.domain.po.CoursePermissionPO;
import com.coursebuddy.domain.vo.CourseActionPermissionVO;
import com.coursebuddy.domain.vo.CourseAdminVoteVO;
import com.coursebuddy.domain.vo.CoursePermissionVO;
import com.coursebuddy.mapper.CourseActionPermissionMapper;
import com.coursebuddy.mapper.CourseAdminVoteMapper;
import com.coursebuddy.mapper.CoursePermissionMapper;
import com.coursebuddy.service.ICoursePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoursePermissionServiceImpl implements ICoursePermissionService {

    private final CoursePermissionMapper permissionRepository;
    private final CourseActionPermissionMapper actionPermissionRepository;
    private final CourseAdminVoteMapper adminVoteRepository;

    private static final Map<String, String> LEVEL_NAMES = Map.of(
            "L1", "课程库管理员",
            "L2", "核心协作成员",
            "L3", "选课班级成员",
            "L4", "校内访客成员"
    );

    private static final Map<String, String> ACTION_NAMES = Map.of(
            "VIEW", "查看",
            "UPLOAD", "上传",
            "EDIT", "编辑",
            "EXPORT", "导出",
            "REVIEW", "审核",
            "TAKEDOWN", "下架",
            "DELETE", "删除",
            "MANAGE_MEMBERS", "成员管理"
    );

    private static final Map<String, Map<String, Boolean>> DEFAULT_MATRIX = defaultMatrix();

    private static Map<String, Map<String, Boolean>> defaultMatrix() {
        Map<String, Map<String, Boolean>> matrix = new LinkedHashMap<>();
        matrix.put("VIEW", Map.of("L1", true, "L2", true, "L3", true, "L4", true));
        matrix.put("UPLOAD", Map.of("L1", true, "L2", true, "L3", true, "L4", false));
        matrix.put("EDIT", Map.of("L1", true, "L2", true, "L3", false, "L4", false));
        matrix.put("EXPORT", Map.of("L1", true, "L2", true, "L3", true, "L4", false));
        matrix.put("REVIEW", Map.of("L1", true, "L2", true, "L3", false, "L4", false));
        matrix.put("TAKEDOWN", Map.of("L1", true, "L2", true, "L3", false, "L4", false));
        matrix.put("DELETE", Map.of("L1", true, "L2", false, "L3", false, "L4", false));
        matrix.put("MANAGE_MEMBERS", Map.of("L1", true, "L2", false, "L3", false, "L4", false));
        return matrix;
    }

    private int levelValue(String level) {
        return switch (level.toUpperCase(Locale.ROOT)) {
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
                .permissionLevel(dto.getPermissionLevel().toUpperCase(Locale.ROOT))
                .grantedBy(currentUser.getId())
                .build();
        permissionRepository.insert(po);
        return toVO(po);
    }

    @Override
    @Transactional
    public CoursePermissionVO updatePermission(CoursePermissionDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        requireAdminOrL1(currentUser, dto.getCourseId());

        CoursePermissionPO po = permissionRepository
                .findByUserIdAndCourseId(dto.getUserId(), dto.getCourseId())
                .orElseThrow(() -> new BusinessException(404, "用户在该课程中没有权限记录"));

        po.setPermissionLevel(dto.getPermissionLevel().toUpperCase(Locale.ROOT));
        po.setGrantedBy(currentUser.getId());
        permissionRepository.updateById(po);
        return toVO(po);
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
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoursePermissionVO> getCourseMembers(Long courseId) {
        return permissionRepository.findByCourseId(courseId).stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoursePermissionVO> getUserPermissions(Long userId) {
        return permissionRepository.findByUserId(userId).stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoursePermissionVO> getCourseAdmins(Long courseId) {
        return permissionRepository.findByCourseIdAndPermissionLevel(courseId, "L1").stream().map(this::toVO).toList();
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

    @Override
    @Transactional(readOnly = true)
    public List<CourseActionPermissionVO> getActionPermissionMatrix(Long courseId) {
        Map<String, Map<String, Boolean>> matrix = mergeWithOverrides(courseId);
        List<CourseActionPermissionVO> rows = new ArrayList<>();
        for (Map.Entry<String, Map<String, Boolean>> entry : matrix.entrySet()) {
            String action = entry.getKey();
            Map<String, Boolean> row = entry.getValue();
            rows.add(CourseActionPermissionVO.builder()
                    .actionKey(action)
                    .actionName(ACTION_NAMES.getOrDefault(action, action))
                    .l1(row.getOrDefault("L1", false))
                    .l2(row.getOrDefault("L2", false))
                    .l3(row.getOrDefault("L3", false))
                    .l4(row.getOrDefault("L4", false))
                    .build());
        }
        return rows;
    }

    @Override
    @Transactional
    public CourseActionPermissionVO updateActionPermission(CourseActionPermissionUpdateDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        requireAdminOrL1(currentUser, dto.getCourseId());

        String level = dto.getPermissionLevel().toUpperCase(Locale.ROOT);
        String action = dto.getActionKey().toUpperCase(Locale.ROOT);

        CourseActionPermissionPO po = actionPermissionRepository
                .findByCourseIdAndPermissionLevelAndActionKey(dto.getCourseId(), level, action)
                .orElseGet(() -> CourseActionPermissionPO.builder()
                        .courseId(dto.getCourseId())
                        .permissionLevel(level)
                        .actionKey(action)
                        .build());
        po.setAllowed(dto.getAllowed());
        po.setUpdatedBy(currentUser.getId());
        if (po.getId() == null) {
            actionPermissionRepository.insert(po);
        } else {
            actionPermissionRepository.updateById(po);
        }

        return getActionPermissionMatrix(dto.getCourseId()).stream()
                .filter(v -> v.getActionKey().equals(action))
                .findFirst()
                .orElseThrow(() -> new BusinessException("权限矩阵更新失败"));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActionPermission(Long courseId, String actionKey) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() == Role.ADMIN) return true;
        String level = getPermissionLevel(courseId);
        if (level == null) return false;
        String action = actionKey.toUpperCase(Locale.ROOT);
        return mergeWithOverrides(courseId)
                .getOrDefault(action, Map.of())
                .getOrDefault(level, false);
    }

    @Override
    @Transactional
    public CourseAdminVoteVO voteAdmin(Long courseId, Long candidateUserId) {
        User currentUser = SecurityUtils.getCurrentUser();
        CoursePermissionPO voterPerm = permissionRepository.findByUserIdAndCourseId(currentUser.getId(), courseId)
                .orElseThrow(() -> new BusinessException(403, "仅课程成员可以参与管理员推选"));
        if ("L4".equals(voterPerm.getPermissionLevel())) {
            throw new BusinessException(403, "访客成员不可参与管理员推选");
        }
        permissionRepository.findByUserIdAndCourseId(candidateUserId, courseId)
                .orElseThrow(() -> new BusinessException(404, "候选人不是该课程成员"));

        if (adminVoteRepository.findByCourseIdAndCandidateUserIdAndVoterUserId(
                courseId, candidateUserId, currentUser.getId()).isPresent()) {
            throw new BusinessException(409, "你已经投过票了");
        }

        adminVoteRepository.insert(CourseAdminVotePO.builder()
                .courseId(courseId)
                .candidateUserId(candidateUserId)
                .voterUserId(currentUser.getId())
                .build());

        return maybePromoteByVotes(courseId, candidateUserId, false);
    }

    @Override
    @Transactional
    public CourseAdminVoteVO rotateAdmin(Long courseId, Long newAdminUserId) {
        User currentUser = SecurityUtils.getCurrentUser();
        requireAdminOrL1(currentUser, courseId);
        permissionRepository.findByUserIdAndCourseId(newAdminUserId, courseId)
                .orElseThrow(() -> new BusinessException(404, "轮值目标不是该课程成员"));
        return maybePromoteByVotes(courseId, newAdminUserId, true);
    }

    private CourseAdminVoteVO maybePromoteByVotes(Long courseId, Long candidateUserId, boolean forceRotate) {
        long totalMembers = permissionRepository.findByCourseId(courseId).stream()
                .map(CoursePermissionPO::getUserId).collect(java.util.stream.Collectors.toSet()).size();
        long votes = adminVoteRepository.countByCourseIdAndCandidateUserId(courseId, candidateUserId);
        long threshold = Math.max(2, (long) Math.ceil(totalMembers * 0.5));

        boolean promoted = forceRotate || votes >= threshold;
        if (promoted) {
            List<CoursePermissionPO> currentAdmins = permissionRepository.findByCourseIdAndPermissionLevel(courseId, "L1");
            for (CoursePermissionPO admin : currentAdmins) {
                if (!admin.getUserId().equals(candidateUserId)) {
                    admin.setPermissionLevel("L2");
                    permissionRepository.updateById(admin);
                }
            }
            CoursePermissionPO candidate = permissionRepository.findByUserIdAndCourseId(candidateUserId, courseId)
                    .orElseThrow(() -> new BusinessException(404, "候选人不存在"));
            candidate.setPermissionLevel("L1");
            permissionRepository.updateById(candidate);
        }

        return CourseAdminVoteVO.builder()
                .courseId(courseId)
                .candidateUserId(candidateUserId)
                .votes(votes)
                .totalMembers(totalMembers)
                .threshold(threshold)
                .promotedToAdmin(promoted)
                .build();
    }

    private Map<String, Map<String, Boolean>> mergeWithOverrides(Long courseId) {
        Map<String, Map<String, Boolean>> merged = new LinkedHashMap<>();
        Set<String> actionKeys = new LinkedHashSet<>(DEFAULT_MATRIX.keySet());
        actionPermissionRepository.findByCourseId(courseId).forEach(p -> actionKeys.add(p.getActionKey()));
        for (String action : actionKeys) {
            Map<String, Boolean> base = new LinkedHashMap<>(DEFAULT_MATRIX.getOrDefault(
                    action, Map.of("L1", true, "L2", false, "L3", false, "L4", false)));
            merged.put(action, base);
        }
        for (CourseActionPermissionPO p : actionPermissionRepository.findByCourseId(courseId)) {
            merged.computeIfAbsent(p.getActionKey(), k -> new LinkedHashMap<>())
                    .put(p.getPermissionLevel(), p.getAllowed());
        }
        return merged;
    }

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
