package com.coursebuddy.service.impl;

import com.coursebuddy.domain.auth.Role;
import com.coursebuddy.domain.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.coursebuddy.domain.dto.AssignmentDTO;
import com.coursebuddy.domain.po.AssignmentPO;
import com.coursebuddy.domain.po.CoursePO;
import com.coursebuddy.domain.vo.AssignmentVO;
import com.coursebuddy.converter.AssignmentConverter;
import com.coursebuddy.mapper.AssignmentMapper;
import com.coursebuddy.mapper.CourseCatalogMapper;
import com.coursebuddy.service.IAssignmentService;
import com.coursebuddy.util.AccessControlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentServiceImpl implements IAssignmentService {

    private final AssignmentMapper assignmentRepository;
    private final AssignmentConverter assignmentMapper;
    private final CourseCatalogMapper courseRepository;
    private final AccessControlValidator accessControlValidator;

    private void checkTeacherOrTA(User user) {
        if (user.getRole() != Role.TEACHER && user.getRole() != Role.TA) {
            throw new BusinessException(403, "Only teachers and TAs can perform this action");
        }
    }

    @Override
    @Transactional
    public AssignmentVO createAssignment(Long courseId, AssignmentDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        checkTeacherOrTA(currentUser);
        // 验证用户对该课程有教学权限（讲师或TA）
        accessControlValidator.validateCourseTeacherAuthority(courseId, currentUser);
        
        AssignmentPO po = assignmentMapper.dtoToPo(dto);
        po.setCourseId(courseId);
        assignmentRepository.insert(po);
        return assignmentMapper.poToVo(po);
    }

    @Override
    @Transactional
    public AssignmentVO updateAssignment(Long id, AssignmentDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        checkTeacherOrTA(currentUser);
        AssignmentPO po = assignmentRepository.selectById(id);
        if (po == null || po.getDeletedAt() != null) {
            throw new BusinessException(404, "Assignment not found");
        }
        // 验证该作业所属课程的教学权限（讲师或TA）
        accessControlValidator.validateCourseTeacherAuthority(po.getCourseId(), currentUser);
        
        if (dto.getTitle() != null) po.setTitle(dto.getTitle());
        if (dto.getDescription() != null) po.setDescription(dto.getDescription());
        if (dto.getDueDate() != null) po.setDueDate(dto.getDueDate());
        if (dto.getMaxScore() != null) po.setMaxScore(dto.getMaxScore());
        if (dto.getAttachmentUrl() != null) po.setAttachmentUrl(dto.getAttachmentUrl());
        assignmentRepository.updateById(po);
        return assignmentMapper.poToVo(po);
    }

    @Override
    @Transactional
    public void deleteAssignment(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        log.info("用户 {} 尝试删除作业 {}", currentUser.getId(), id);
        checkTeacherOrTA(currentUser);
        AssignmentPO po = assignmentRepository.selectById(id);
        if (po == null || po.getDeletedAt() != null) {
            log.warn("作业 {} 不存在或已被删除", id);
            throw new BusinessException(404, "Assignment not found");
        }
        // 验证该作业所属课程的教学权限（讲师或TA）
        accessControlValidator.validateCourseTeacherAuthority(po.getCourseId(), currentUser);
        
        LocalDateTime deletedAt = LocalDateTime.now();
        log.info("开始删除作业 {}，设置 deletedAt 为 {}", id, deletedAt);
        po.setDeletedAt(deletedAt);
        int updateResult = assignmentRepository.markDeleted(id, deletedAt);
        log.info("作业 {} 删除完成，更新结果: {}", id, updateResult);
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentVO getAssignment(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        AssignmentPO po = assignmentRepository.selectById(id);
        if (po == null || po.getDeletedAt() != null) {
            throw new BusinessException(404, "Assignment not found");
        }
        // 验证用户是该课程的成员
        accessControlValidator.validateCourseMember(po.getCourseId(), currentUser.getId());
        
        return assignmentMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssignmentVO> listAssignments(Long courseId, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        log.info("用户 {} 查询课程 {} 的作业列表, 分页参数: {}", currentUser.getId(), courseId, pageable);
        
        // 验证用户是课程成员（讲师或学生）
        accessControlValidator.validateCourseMember(courseId, currentUser.getId());
        
        // 使用自定义查询，确保只返回未删除的作业
        log.info("执行数据库查询: WHERE course_id = {} AND deleted_at IS NULL", courseId);
        IPage<AssignmentPO> poPage = assignmentRepository.findByCourseIdAndDeletedAtIsNull(
                MybatisPlusPageUtils.toMpPage(pageable), courseId);
        log.info("查询完成，共返回 {} 条记录, 总数: {}", poPage.getRecords().size(), poPage.getTotal());
        
        return assignmentMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional
    public AssignmentVO publishAssignment(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        checkTeacherOrTA(currentUser);
        AssignmentPO po = assignmentRepository.selectById(id);
        if (po == null || po.getDeletedAt() != null) {
            throw new BusinessException(404, "Assignment not found");
        }
        // 验证该作业所属课程的教学权限（讲师或TA）
        accessControlValidator.validateCourseTeacherAuthority(po.getCourseId(), currentUser);
        
        po.setIsPublished(true);
        assignmentRepository.updateById(po);
        return assignmentMapper.poToVo(po);
    }

    @Override
    public List<Object> getAllAssignmentsIncludingDeleted(Long courseId) {
        log.info("📋 DEBUG: 查询课程 {} 的所有作业（包括已删除的）", courseId);
        return assignmentRepository.findAllByCourseIdIncludingDeleted(courseId).stream()
                .map(po -> {
                    return java.util.Map.of(
                        "id", po.getId(),
                        "title", po.getTitle(),
                        "deletedAt", po.getDeletedAt(),
                        "createdAt", po.getCreatedAt()
                    );
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
