package com.coursebuddy.service.impl;

import com.coursebuddy.auth.Role;
import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.CourseDTO;
import com.coursebuddy.domain.po.CoursePO;
import com.coursebuddy.domain.vo.CourseStatsVO;
import com.coursebuddy.domain.vo.CourseVO;
import com.coursebuddy.mapper.CourseMapper;
import com.coursebuddy.repository.AssignmentRepository;
import com.coursebuddy.repository.CourseCatalogRepository;
import com.coursebuddy.repository.CourseEnrollmentRepository;
import com.coursebuddy.repository.LessonRepository;
import com.coursebuddy.service.ICourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements ICourseService {

    private final CourseCatalogRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final AssignmentRepository assignmentRepository;
    private final CourseMapper courseMapper;

    @Override
    @Transactional
    public CourseVO createCourse(CourseDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() != Role.TEACHER && currentUser.getRole() != Role.ADMIN) {
            throw new BusinessException(403, "Only teachers and admins can create courses");
        }
        CoursePO po = courseMapper.dtoToPo(dto);
        po.setInstructorId(currentUser.getId());
        return courseMapper.poToVo(courseRepository.save(po));
    }

    @Override
    @Transactional
    public CourseVO updateCourse(Long id, CourseDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        CoursePO po = courseRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(404, "Course not found"));
        if (currentUser.getRole() != Role.ADMIN && !po.getInstructorId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Not authorized to update this course");
        }
        if (dto.getCode() != null) po.setCode(dto.getCode());
        if (dto.getName() != null) po.setName(dto.getName());
        if (dto.getDescription() != null) po.setDescription(dto.getDescription());
        if (dto.getCreditHours() != null) po.setCreditHours(dto.getCreditHours());
        if (dto.getLevel() != null) po.setLevel(dto.getLevel());
        if (dto.getCapacity() != null) po.setCapacity(dto.getCapacity());
        if (dto.getThumbnailUrl() != null) po.setThumbnailUrl(dto.getThumbnailUrl());
        if (dto.getSyllabus() != null) po.setSyllabus(dto.getSyllabus());
        if (dto.getMaxGrade() != null) po.setMaxGrade(dto.getMaxGrade());
        if (dto.getPassingGrade() != null) po.setPassingGrade(dto.getPassingGrade());
        if (dto.getStartDate() != null) po.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) po.setEndDate(dto.getEndDate());
        if (dto.getDepartmentId() != null) po.setDepartmentId(dto.getDepartmentId());
        return courseMapper.poToVo(courseRepository.save(po));
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        CoursePO po = courseRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(404, "Course not found"));
        if (currentUser.getRole() != Role.ADMIN && !po.getInstructorId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Not authorized to delete this course");
        }
        po.setDeletedAt(LocalDateTime.now());
        courseRepository.save(po);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseVO getCourse(Long id) {
        CoursePO po = courseRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(404, "Course not found"));
        return courseMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseVO> listAllCourses(Pageable pageable) {
        return courseMapper.poPageToVoPage(courseRepository.findByDeletedAtIsNull(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseVO> listMyTeachingCourses(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        return courseMapper.poPageToVoPage(
                courseRepository.findByInstructorIdAndDeletedAtIsNull(currentUser.getId(), pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseVO> searchCourses(String keyword, String level, Pageable pageable) {
        return courseMapper.poPageToVoPage(courseRepository.searchCourses(keyword, level, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseStatsVO getCourseStats(Long courseId) {
        long totalEnrollments = enrollmentRepository.countByCourseId(courseId);
        long activeEnrollments = enrollmentRepository.findByCourseIdAndStatus(courseId, "ACTIVE",
                org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        long completedEnrollments = enrollmentRepository.findByCourseIdAndStatus(courseId, "COMPLETED",
                org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        long totalLessons = lessonRepository.countByCourseIdAndDeletedAtIsNull(courseId);
        long publishedLessons = lessonRepository.countByCourseIdAndIsPublishedTrueAndDeletedAtIsNull(courseId);
        long totalAssignments = assignmentRepository.countByCourseIdAndDeletedAtIsNull(courseId);
        return CourseStatsVO.builder()
                .courseId(courseId)
                .totalEnrollments(totalEnrollments)
                .activeEnrollments(activeEnrollments)
                .completedEnrollments(completedEnrollments)
                .totalLessons(totalLessons)
                .publishedLessons(publishedLessons)
                .totalAssignments(totalAssignments)
                // TODO: calculate actual average progress from learning progress records
                .averageProgress(0.0)
                .build();
    }
}
