package com.coursebuddy.service.impl;

import com.coursebuddy.domain.auth.Role;
import com.coursebuddy.domain.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.CourseEnrollmentDTO;
import com.coursebuddy.domain.po.CourseEnrollmentPO;
import com.coursebuddy.domain.po.CoursePO;
import com.coursebuddy.domain.vo.CourseEnrollmentVO;
import com.coursebuddy.converter.CourseEnrollmentConverter;
import com.coursebuddy.mapper.CourseCatalogMapper;
import com.coursebuddy.mapper.CourseEnrollmentMapper;
import com.coursebuddy.service.ICourseEnrollmentService;
import com.coursebuddy.util.AccessControlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 课程选课服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseEnrollmentServiceImpl implements ICourseEnrollmentService {

    private final CourseEnrollmentMapper enrollmentRepository;
    private final CourseEnrollmentConverter enrollmentMapper;
    private final CourseCatalogMapper courseRepository;
    private final AccessControlValidator accessControlValidator;

    @Override
    @Transactional
    public CourseEnrollmentVO enroll(CourseEnrollmentDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        ensureStudent(currentUser);
        CoursePO course = courseRepository.selectById(dto.getCourseId());
        if (course == null || course.getDeletedAt() != null) {
            throw new BusinessException(404, "Course not found");
        }
        validateCourseJoinable(course, currentUser.getId());
        CourseEnrollmentPO po = CourseEnrollmentPO.builder()
                .courseId(dto.getCourseId())
                .userId(currentUser.getId())
                .status("ACTIVE")
                .enrolledAt(LocalDateTime.now())
                .build();
        enrollmentRepository.insert(po);
        courseRepository.incrementEnrolledCount(dto.getCourseId());
        return enrollmentMapper.poToVo(po);
    }

    @Override
    @Transactional
    public CourseEnrollmentVO enrollByCode(String code) {
        log.info("用户尝试通过邀请码加入课堂: code={}", code);
        User currentUser = SecurityUtils.getCurrentUser();
        ensureStudent(currentUser);
        log.info("当前用户: id={}, username={}", currentUser.getId(), currentUser.getUsername());
        
        // 1. 根据代码查找课程
        CoursePO course = courseRepository.findByCode(code)
                .orElseThrow(() -> {
                    log.warn("无效的课堂邀请码: {}", code);
                    return new BusinessException(404, "课堂邀请码无效");
                });

        if (course.getDeletedAt() != null) {
            throw new BusinessException(404, "课堂不存在或已删除");
        }
        
        log.info("查找到对应课程: id={}, name={}", course.getId(), course.getName());
        
        validateCourseJoinable(course, currentUser.getId());
        
        // 4. 创建选课记录
        CourseEnrollmentPO po = CourseEnrollmentPO.builder()
                .courseId(course.getId())
                .userId(currentUser.getId())
                .status("ACTIVE")
                .enrolledAt(LocalDateTime.now())
                .build();
        enrollmentRepository.insert(po);
        log.info("选课记录已插入: enrollmentId={}", po.getId());
        
        // 5. 更新课程选课人数
        courseRepository.incrementEnrolledCount(course.getId());
        log.info("课程选课人数已更新: courseId={}", course.getId());
        
        return enrollmentMapper.poToVo(po);
    }

    @Override
    @Transactional
    public void drop(Long courseId) {
        User currentUser = SecurityUtils.getCurrentUser();
        ensureStudent(currentUser);
        CourseEnrollmentPO po = enrollmentRepository.findByCourseIdAndUserId(courseId, currentUser.getId())
                .orElseThrow(() -> new BusinessException(404, "Enrollment not found"));
        if (!"ACTIVE".equals(po.getStatus())) {
            throw new BusinessException(400, "Only active enrollments can be dropped");
        }
        po.setStatus("DROPPED");
        po.setDroppedAt(LocalDateTime.now());
        enrollmentRepository.updateById(po);
        courseRepository.decrementEnrolledCount(courseId);
    }

    @Override
    @Transactional
    public void complete(Long courseId) {
        User currentUser = SecurityUtils.getCurrentUser();
        ensureStudent(currentUser);
        CourseEnrollmentPO po = enrollmentRepository.findByCourseIdAndUserId(courseId, currentUser.getId())
                .orElseThrow(() -> new BusinessException(404, "Enrollment not found"));
        if (!"ACTIVE".equals(po.getStatus())) {
            throw new BusinessException(400, "Only active enrollments can be completed");
        }
        po.setStatus("COMPLETED");
        po.setCompletedAt(LocalDateTime.now());
        enrollmentRepository.updateById(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseEnrollmentVO> listMyEnrollments(String status, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (status != null) {
            IPage<CourseEnrollmentPO> poPage = enrollmentRepository.findByUserIdAndStatus(
                    MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId(), status);
            return enrollmentMapper.poPageToVoPage(
                    MybatisPlusPageUtils.toSpringPage(poPage, pageable));
        }
        IPage<CourseEnrollmentPO> poPage = enrollmentRepository.findByUserId(
                MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId());
        return enrollmentMapper.poPageToVoPage(
                MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseEnrollmentVO> listCourseStudents(Long courseId, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        // 验证当前用户是否可以查看该课程的学生列表
        // 仅允许讲师、TA或该课程的学生查看
        try {
            accessControlValidator.validateCourseInstructor(courseId, currentUser.getId());
        } catch (BusinessException e) {
            // 不是讲师，检查是否是学生
            accessControlValidator.validateCourseMember(courseId, currentUser.getId());
        }
        
        IPage<CourseEnrollmentPO> poPage = enrollmentRepository.findByCourseId(
                MybatisPlusPageUtils.toMpPage(pageable), courseId);
        return enrollmentMapper.poPageToVoPageWithUsers(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
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

    private void ensureStudent(User currentUser) {
        if (currentUser.getRole() != Role.STUDENT) {
            throw new BusinessException(403, "Only students can enroll in courses");
        }
    }

    private void validateCourseJoinable(CoursePO course, Long userId) {
        if (!"OPEN".equalsIgnoreCase(course.getStatus())) {
            throw new BusinessException(400, "Course is not open for enrollment");
        }
        if (enrollmentRepository.existsByCourseIdAndUserId(course.getId(), userId)) {
            log.warn("用户已在该课堂中: userId={}, courseId={}", userId, course.getId());
            throw new BusinessException(409, "您已经在这个课堂中了");
        }
        if (course.getEnrolledCount() >= course.getCapacity()) {
            log.warn("课堂已满: courseId={}, enrolled={}, capacity={}", course.getId(), course.getEnrolledCount(), course.getCapacity());
            throw new BusinessException(400, "该课堂已达到人数上限");
        }
    }
}
