package com.coursebuddy.service.impl;

import com.coursebuddy.domain.auth.Role;
import com.coursebuddy.domain.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.AssignmentSubmissionDTO;
import com.coursebuddy.domain.dto.GradeSubmissionDTO;
import com.coursebuddy.domain.po.AssignmentPO;
import com.coursebuddy.domain.po.AssignmentSubmissionPO;
import com.coursebuddy.domain.po.UserPO;
import com.coursebuddy.domain.vo.AssignmentSubmissionVO;
import com.coursebuddy.domain.vo.AssignmentSubmissionCountVO;
import com.coursebuddy.converter.AssignmentSubmissionConverter;
import com.coursebuddy.mapper.AssignmentMapper;
import com.coursebuddy.mapper.AssignmentSubmissionMapper;
import com.coursebuddy.mapper.CourseCatalogMapper;
import com.coursebuddy.mapper.UserMapper;
import com.coursebuddy.service.IAssignmentSubmissionService;
import com.coursebuddy.util.AccessControlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 作业提交服务实现
 */
@Service
@RequiredArgsConstructor
public class AssignmentSubmissionServiceImpl implements IAssignmentSubmissionService {

    private final AssignmentSubmissionMapper submissionRepository;
    private final AssignmentMapper assignmentRepository;
    private final AssignmentSubmissionConverter submissionMapper;
    private final CourseCatalogMapper courseRepository;
    private final AccessControlValidator accessControlValidator;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AssignmentSubmissionVO submitAssignment(Long assignmentId, AssignmentSubmissionDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (!Role.STUDENT.equals(currentUser.getRole())) {
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
        User currentUser = SecurityUtils.getCurrentUser();
        // 验证该作业是否属于当前用户的课程
        AssignmentPO assignment = assignmentRepository.selectById(assignmentId);
        if (assignment == null) {
            throw new BusinessException(404, "Assignment not found");
        }
        accessControlValidator.validateCourseTeacherAuthority(assignment.getCourseId(), currentUser);
        
        IPage<AssignmentSubmissionPO> poPage = submissionRepository.findByAssignmentId(
                MybatisPlusPageUtils.toMpPage(pageable), assignmentId);
        Page<AssignmentSubmissionVO> voPage = submissionMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
        List<Long> studentIds = poPage.getRecords().stream()
                .map(AssignmentSubmissionPO::getStudentId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> nameMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<UserPO> users = userMapper.selectBatchIds(studentIds);
            for (UserPO user : users) {
                String name = user.getRealName() != null && !user.getRealName().isBlank()
                        ? user.getRealName()
                        : user.getUsername();
                nameMap.put(user.getId(), name);
            }
        }
        voPage.getContent().forEach(vo -> vo.setStudentName(nameMap.get(vo.getStudentId())));
        return voPage;
    }

    @Override
    @Transactional
    public AssignmentSubmissionVO gradeSubmission(Long id, GradeSubmissionDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (!Role.TEACHER.equals(currentUser.getRole()) && !Role.TA.equals(currentUser.getRole())) {
            throw new BusinessException(403, "Only teachers and TAs can grade submissions");
        }
        AssignmentSubmissionPO po = submissionRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Submission not found");
        }
        
        // 验证该作业提交所属的作业是否属于当前讲师的课程
        AssignmentPO assignment = assignmentRepository.selectById(po.getAssignmentId());
        if (assignment == null) {
            throw new BusinessException(404, "Assignment not found");
        }
        accessControlValidator.validateCourseInstructor(assignment.getCourseId(), currentUser.getId());
        
        po.setScore(dto.getScore());
        po.setFeedback(dto.getComment());
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
            .orElse(null);
        return submissionMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentSubmissionVO> listMySubmissionsByCourse(Long courseId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (!Role.STUDENT.equals(currentUser.getRole())) {
            throw new BusinessException(403, "Only students can view their submissions");
        }
        List<AssignmentSubmissionPO> submissions = submissionRepository.findByCourseIdAndStudentId(courseId, currentUser.getId());
        return submissionMapper.poListToVoList(submissions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentSubmissionCountVO> listSubmissionCountsByCourse(Long courseId) {
        User currentUser = SecurityUtils.getCurrentUser();
        accessControlValidator.validateCourseTeacherAuthority(courseId, currentUser);
        return submissionRepository.findSubmissionCountsByCourse(courseId);
    }

    @Override
    public Resource downloadAllSubmissions(Long assignmentId) {
        List<AssignmentSubmissionPO> submissions = submissionRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AssignmentSubmissionPO>()
                        .eq(AssignmentSubmissionPO::getAssignmentId, assignmentId)
        );

        if (submissions.isEmpty()) {
            throw new BusinessException(404, "No submissions found for this assignment");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (AssignmentSubmissionPO sub : submissions) {
                // 这里假设 submissionUrl 是文件的内容或者我们可以获取到的内容
                // 实际生产中可能需要从 MinIO/S3 下载
                String content = "Submission Content: " + sub.getSubmissionUrl(); 
                ZipEntry entry = new ZipEntry("student_" + sub.getStudentId() + "_submission.txt");
                zos.putNextEntry(entry);
                zos.write(content.getBytes());
                zos.closeEntry();
            }
            zos.finish();
            return new ByteArrayResource(baos.toByteArray());
        } catch (IOException e) {
            throw new BusinessException(500, "Failed to create zip file");
        }
    }
}
