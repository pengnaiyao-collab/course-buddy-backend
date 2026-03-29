package com.coursebuddy.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.auth.Role;
import com.coursebuddy.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.CourseDTO;
import com.coursebuddy.domain.po.CoursePO;
import com.coursebuddy.domain.vo.CourseStatsVO;
import com.coursebuddy.domain.vo.CourseVO;
import com.coursebuddy.converter.CourseConverter;
import com.coursebuddy.mapper.AssignmentMapper;
import com.coursebuddy.mapper.CourseCatalogMapper;
import com.coursebuddy.mapper.CourseEnrollmentMapper;
import com.coursebuddy.mapper.LessonMapper;
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

    private final CourseCatalogMapper courseRepository;
    private final CourseEnrollmentMapper enrollmentRepository;
    private final LessonMapper lessonRepository;
    private final AssignmentMapper assignmentRepository;
    private final CourseConverter courseMapper;

    @Override
    @Transactional
    public CourseVO createCourse(CourseDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() != Role.TEACHER && currentUser.getRole() != Role.ADMIN) {
            throw new BusinessException(403, "Only teachers and admins can create courses");
        }
        CoursePO po = courseMapper.dtoToPo(dto);
        po.setInstructorId(currentUser.getId());
        courseRepository.insert(po);
        return courseMapper.poToVo(po);
    }

    @Override
    @Transactional
    public CourseVO updateCourse(Long id, CourseDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        CoursePO po = courseRepository.selectById(id);
        if (po == null || po.getDeletedAt() != null) {
            throw new BusinessException(404, "Course not found");
        }
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
        courseRepository.updateById(po);
        return courseMapper.poToVo(po);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        CoursePO po = courseRepository.selectById(id);
        if (po == null || po.getDeletedAt() != null) {
            throw new BusinessException(404, "Course not found");
        }
        if (currentUser.getRole() != Role.ADMIN && !po.getInstructorId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Not authorized to delete this course");
        }
        po.setDeletedAt(LocalDateTime.now());
        courseRepository.updateById(po);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseVO getCourse(Long id) {
        CoursePO po = courseRepository.selectById(id);
        if (po == null || po.getDeletedAt() != null) {
            throw new BusinessException(404, "Course not found");
        }
        return courseMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseVO> listAllCourses(Pageable pageable) {
        IPage<CoursePO> poPage = courseRepository.findByDeletedAtIsNull(MybatisPlusPageUtils.toMpPage(pageable));
        return courseMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseVO> listMyTeachingCourses(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        IPage<CoursePO> poPage = courseRepository.findByInstructorIdAndDeletedAtIsNull(
                MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId());
        return courseMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseVO> searchCourses(String keyword, String level, Pageable pageable) {
        IPage<CoursePO> poPage = courseRepository.searchCourses(
                MybatisPlusPageUtils.toMpPage(pageable), keyword, level);
        return courseMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseStatsVO getCourseStats(Long courseId) {
        long totalEnrollments = enrollmentRepository.countByCourseId(courseId);
        long activeEnrollments = enrollmentRepository.findByCourseIdAndStatus(
                MybatisPlusPageUtils.toMpPage(org.springframework.data.domain.Pageable.unpaged()),
                courseId, "ACTIVE").getTotal();
        long completedEnrollments = enrollmentRepository.findByCourseIdAndStatus(
                MybatisPlusPageUtils.toMpPage(org.springframework.data.domain.Pageable.unpaged()),
                courseId, "COMPLETED").getTotal();
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
                .averageProgress(null)
                .build();
    }
}
