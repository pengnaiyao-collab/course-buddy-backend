package com.coursebuddy.service.impl;

import com.coursebuddy.auth.Role;
import com.coursebuddy.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.AssignmentSubmissionDTO;
import com.coursebuddy.domain.dto.GradeSubmissionDTO;
import com.coursebuddy.domain.po.AssignmentPO;
import com.coursebuddy.domain.po.AssignmentSubmissionPO;
import com.coursebuddy.domain.vo.AssignmentSubmissionVO;
import com.coursebuddy.converter.AssignmentSubmissionConverter;
import com.coursebuddy.mapper.AssignmentMapper;
import com.coursebuddy.mapper.AssignmentSubmissionMapper;
import com.coursebuddy.service.IAssignmentSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssignmentSubmissionServiceImpl implements IAssignmentSubmissionService {

    private final AssignmentSubmissionMapper submissionRepository;
    private final AssignmentMapper assignmentRepository;
    private final AssignmentSubmissionConverter submissionMapper;

    @Override
    @Transactional
    public AssignmentSubmissionVO submitAssignment(Long assignmentId, AssignmentSubmissionDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() != Role.STUDENT) {
            throw new BusinessException(403, "Only students can submit assignments");
        }
        if (submissionRepository.findByAssignmentIdAndStudentId(assignmentId, currentUser.getId()).isPresent()) {
            throw new BusinessException(409, "You have already submitted this assignment");
        }
        AssignmentPO assignment = assignmentRepository.selectById(assignmentId);
        if (assignment == null || assignment.getDeletedAt() != null) {
            throw new BusinessException(404, "Assignment not found");
        }

        String status = "SUBMITTED";
        if (assignment.getDueDate() != null && LocalDateTime.now().isAfter(assignment.getDueDate())) {
            status = "LATE";
        }

        AssignmentSubmissionPO po = AssignmentSubmissionPO.builder()
                .assignmentId(assignmentId)
                .studentId(currentUser.getId())
                .submissionUrl(dto.getSubmissionUrl())
                .submittedAt(LocalDateTime.now())
                .status(status)
                .build();
        submissionRepository.insert(po);
        return submissionMapper.poToVo(po);
    }

    @Override
    @Transactional
    public AssignmentSubmissionVO updateSubmission(Long id, AssignmentSubmissionDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        AssignmentSubmissionPO po = submissionRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Submission not found");
        }
        if (!po.getStudentId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Not authorized to update this submission");
        }
        po.setSubmissionUrl(dto.getSubmissionUrl());
        submissionRepository.updateById(po);
        return submissionMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentSubmissionVO getSubmission(Long id) {
        AssignmentSubmissionPO po = submissionRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Submission not found");
        }
        return submissionMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssignmentSubmissionVO> listSubmissions(Long assignmentId, Pageable pageable) {
        IPage<AssignmentSubmissionPO> poPage = submissionRepository.findByAssignmentId(
                MybatisPlusPageUtils.toMpPage(pageable), assignmentId);
        return submissionMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional
    public AssignmentSubmissionVO gradeSubmission(Long id, GradeSubmissionDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() != Role.TEACHER && currentUser.getRole() != Role.ADMIN) {
            throw new BusinessException(403, "Only teachers and admins can grade submissions");
        }
        AssignmentSubmissionPO po = submissionRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Submission not found");
        }
        po.setScore(dto.getScore());
        po.setFeedback(dto.getFeedback());
        po.setGradedAt(LocalDateTime.now());
        po.setGradedBy(currentUser.getId());
        po.setStatus("GRADED");
        submissionRepository.updateById(po);
        return submissionMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentSubmissionVO getMySubmission(Long assignmentId) {
        User currentUser = SecurityUtils.getCurrentUser();
        AssignmentSubmissionPO po = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, currentUser.getId())
                .orElseThrow(() -> new BusinessException(404, "Submission not found"));
        return submissionMapper.poToVo(po);
    }
}
