package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.CourseEnrollmentDTO;
import com.coursebuddy.domain.po.CourseEnrollmentPO;
import com.coursebuddy.domain.vo.CourseEnrollmentVO;
import com.coursebuddy.mapper.CourseEnrollmentMapper;
import com.coursebuddy.repository.CourseEnrollmentRepository;
import com.coursebuddy.service.ICourseEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentServiceImpl implements ICourseEnrollmentService {

    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseEnrollmentMapper enrollmentMapper;

    @Override
    @Transactional
    public CourseEnrollmentVO enroll(CourseEnrollmentDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (enrollmentRepository.existsByCourseIdAndUserId(dto.getCourseId(), currentUser.getId())) {
            throw new BusinessException(409, "Already enrolled in this course");
        }
        CourseEnrollmentPO po = CourseEnrollmentPO.builder()
                .courseId(dto.getCourseId())
                .userId(currentUser.getId())
                .status("ACTIVE")
                .build();
        return enrollmentMapper.poToVo(enrollmentRepository.save(po));
    }

    @Override
    @Transactional
    public void drop(Long courseId) {
        User currentUser = SecurityUtils.getCurrentUser();
        CourseEnrollmentPO po = enrollmentRepository.findByCourseIdAndUserId(courseId, currentUser.getId())
                .orElseThrow(() -> new BusinessException(404, "Enrollment not found"));
        po.setStatus("DROPPED");
        po.setDroppedAt(LocalDateTime.now());
        enrollmentRepository.save(po);
    }

    @Override
    @Transactional
    public void complete(Long courseId) {
        User currentUser = SecurityUtils.getCurrentUser();
        CourseEnrollmentPO po = enrollmentRepository.findByCourseIdAndUserId(courseId, currentUser.getId())
                .orElseThrow(() -> new BusinessException(404, "Enrollment not found"));
        po.setStatus("COMPLETED");
        po.setCompletedAt(LocalDateTime.now());
        enrollmentRepository.save(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseEnrollmentVO> listMyEnrollments(String status, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (status != null) {
            return enrollmentMapper.poPageToVoPage(
                    enrollmentRepository.findByUserIdAndStatus(currentUser.getId(), status, pageable));
        }
        return enrollmentMapper.poPageToVoPage(
                enrollmentRepository.findByUserId(currentUser.getId(), pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseEnrollmentVO> listCourseStudents(Long courseId, Pageable pageable) {
        return enrollmentMapper.poPageToVoPage(
                enrollmentRepository.findByCourseId(courseId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEnrolled(Long courseId) {
        User currentUser = SecurityUtils.getCurrentUser();
        return enrollmentRepository.existsByCourseIdAndUserId(courseId, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public long countCourseStudents(Long courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }
}
