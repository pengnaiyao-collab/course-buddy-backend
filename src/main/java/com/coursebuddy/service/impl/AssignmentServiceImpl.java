package com.coursebuddy.service.impl;

import com.coursebuddy.auth.Role;
import com.coursebuddy.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.AssignmentDTO;
import com.coursebuddy.domain.po.AssignmentPO;
import com.coursebuddy.domain.vo.AssignmentVO;
import com.coursebuddy.converter.AssignmentConverter;
import com.coursebuddy.mapper.AssignmentMapper;
import com.coursebuddy.service.IAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements IAssignmentService {

    private final AssignmentMapper assignmentRepository;
    private final AssignmentConverter assignmentMapper;

    private void checkTeacherOrAdmin(User user) {
        if (user.getRole() != Role.TEACHER && user.getRole() != Role.ADMIN) {
            throw new BusinessException(403, "Only teachers and admins can perform this action");
        }
    }

    @Override
    @Transactional
    public AssignmentVO createAssignment(Long courseId, AssignmentDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        checkTeacherOrAdmin(currentUser);
        AssignmentPO po = assignmentMapper.dtoToPo(dto);
        po.setCourseId(courseId);
        assignmentRepository.insert(po);
        return assignmentMapper.poToVo(po);
    }

    @Override
    @Transactional
    public AssignmentVO updateAssignment(Long id, AssignmentDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        checkTeacherOrAdmin(currentUser);
        AssignmentPO po = assignmentRepository.selectById(id);
        if (po == null || po.getDeletedAt() != null) {
            throw new BusinessException(404, "Assignment not found");
        }
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
        checkTeacherOrAdmin(currentUser);
        AssignmentPO po = assignmentRepository.selectById(id);
        if (po == null || po.getDeletedAt() != null) {
            throw new BusinessException(404, "Assignment not found");
        }
        po.setDeletedAt(LocalDateTime.now());
        assignmentRepository.updateById(po);
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentVO getAssignment(Long id) {
        AssignmentPO po = assignmentRepository.selectById(id);
        if (po == null || po.getDeletedAt() != null) {
            throw new BusinessException(404, "Assignment not found");
        }
        return assignmentMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssignmentVO> listAssignments(Long courseId, Pageable pageable) {
        IPage<AssignmentPO> poPage = assignmentRepository.findByCourseIdAndDeletedAtIsNull(
                MybatisPlusPageUtils.toMpPage(pageable), courseId);
        return assignmentMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional
    public AssignmentVO publishAssignment(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        checkTeacherOrAdmin(currentUser);
        AssignmentPO po = assignmentRepository.selectById(id);
        if (po == null || po.getDeletedAt() != null) {
            throw new BusinessException(404, "Assignment not found");
        }
        po.setIsPublished(true);
        assignmentRepository.updateById(po);
        return assignmentMapper.poToVo(po);
    }
}
