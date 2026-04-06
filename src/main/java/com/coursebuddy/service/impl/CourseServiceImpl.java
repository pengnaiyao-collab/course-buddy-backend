package com.coursebuddy.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.auth.Role;
import com.coursebuddy.domain.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.CourseDTO;
import com.coursebuddy.domain.po.*;
import com.coursebuddy.domain.vo.CourseStatsVO;
import com.coursebuddy.domain.vo.CourseVO;
import com.coursebuddy.domain.vo.SimplifiedStatsVO;
import com.coursebuddy.converter.CourseConverter;
import com.coursebuddy.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coursebuddy.service.ICourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 课程服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements ICourseService {

    private final CourseCatalogMapper courseRepository;
    private final CourseEnrollmentMapper enrollmentRepository;
    private final LessonMapper lessonRepository;
    private final AssignmentMapper assignmentRepository;
    private final CourseDiscussionMapper discussionRepository;
    private final AssignmentSubmissionMapper submissionRepository;
    private final CourseConverter courseMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    private void fillExtraInfo(CourseVO vo, Long currentUserId) {
        if (vo == null) return;
        
        // 1. 填充讲师姓名
        if (vo.getInstructorId() != null) {
            UserPO instructor = userMapper.selectById(vo.getInstructorId());
            if (instructor != null) {
                vo.setInstructorName(instructor.getRealName() != null ? instructor.getRealName() : instructor.getUsername());
            }
        }
        
        // 2. 填充选课状态
        if (currentUserId != null) {
            boolean isEnrolled = enrollmentRepository.existsByCourseIdAndUserId(vo.getId(), currentUserId);
            vo.setEnrolled(isEnrolled);
        } else {
            vo.setEnrolled(false);
        }
    }

    @Override
    @Transactional
    public CourseVO createCourse(CourseDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (!Role.TEACHER.equals(currentUser.getRole())) {
            throw new BusinessException(403, "Only teachers can create courses");
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
        if (!Role.TEACHER.equals(currentUser.getRole()) || !Objects.equals(po.getInstructorId(), currentUser.getId())) {
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
        
        // 管理员可以删除任何课程，教师只能删除自己的课程
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isCourseInstructor = po.getInstructorId().equals(currentUser.getId());
        
        if (!isAdmin && !isCourseInstructor) {
            throw new BusinessException(403, "Not authorized to delete this course");
        }

        LocalDateTime deletedAt = LocalDateTime.now();
        log.info("开始删除课程 {}，设置 deletedAt 为 {}", id, deletedAt);
        po.setDeletedAt(deletedAt);
        int updateResult = courseRepository.markDeleted(id, deletedAt);
        log.info("课程 {} 删除完成，更新结果: {}", id, updateResult);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseVO getCourse(Long id) {
        CoursePO po = courseRepository.selectById(id);
        if (po == null || po.getDeletedAt() != null) {
            throw new BusinessException(404, "Course not found");
        }
        CourseVO vo = courseMapper.poToVo(po);
        
        User currentUser = SecurityUtils.getOptionalUser();
        fillExtraInfo(vo, currentUser != null ? currentUser.getId() : null);
        
        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseVO> listAllCourses(Pageable pageable) {
        IPage<CoursePO> poPage = courseRepository.findByDeletedAtIsNull(MybatisPlusPageUtils.toMpPage(pageable));
        Page<CourseVO> voPage = courseMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
        
        User currentUser = SecurityUtils.getOptionalUser();
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        
        for (CourseVO vo : voPage) {
            fillExtraInfo(vo, currentUserId);
        }
        return voPage;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseVO> listMyTeachingCourses(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        IPage<CoursePO> poPage = courseRepository.findByInstructorIdAndDeletedAtIsNull(
                MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId());
        return MybatisPlusPageUtils.toSpringPage(poPage, pageable).map(courseMapper::poToVo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseVO> listMyEnrolledCourses(Pageable pageable) {
        log.info("用户请求查询已选课程列表: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        User currentUser = SecurityUtils.getCurrentUser();
        log.info("当前用户: id={}, username={}, role={}", currentUser.getId(), currentUser.getUsername(), currentUser.getRole());
        
        java.util.List<CoursePO> pos = courseRepository.findByStudentIdAndDeletedAtIsNull(currentUser.getId());
        log.info("查找到已选课程总数: {}", pos.size());
        
        // 手动分页处理
        int start = (int) pageable.getOffset();
        if (start >= pos.size() && pos.size() > 0) {
            log.info("偏移量超出列表长度: offset={}, size={}", start, pos.size());
            return new org.springframework.data.domain.PageImpl<>(java.util.Collections.emptyList(), pageable, pos.size());
        }
        
        if (pos.isEmpty()) {
            log.info("已选课程列表为空");
            return new org.springframework.data.domain.PageImpl<>(java.util.Collections.emptyList(), pageable, 0);
        }

        int end = Math.min((start + pageable.getPageSize()), pos.size());
        log.info("分页区间: [{}, {})", start, end);
        java.util.List<CoursePO> pagedPos = pos.subList(start, end);
        java.util.List<CourseVO> vos = courseMapper.poListToVoList(pagedPos);
        
        // 已选课程列表中的项显然都是已选的
        vos.forEach(vo -> vo.setEnrolled(true));
                
        return new org.springframework.data.domain.PageImpl<>(vos, pageable, pos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseVO> searchCourses(String keyword, String level, Pageable pageable) {
        IPage<CoursePO> poPage = courseRepository.searchCourses(
                MybatisPlusPageUtils.toMpPage(pageable), keyword, level);
        Page<CourseVO> voPage = courseMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
        
        User currentUser = SecurityUtils.getOptionalUser();
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        
        for (CourseVO vo : voPage) {
            fillExtraInfo(vo, currentUserId);
        }
        return voPage;
    }

    @Override
    @Transactional(readOnly = true)
    public CourseStatsVO getCourseStats(Long courseId) {
        // 提供一个基础实现（如未使用可直接抛出异常）
        return CourseStatsVO.builder().courseId(courseId).build();
    }

    @Override
    @Transactional(readOnly = true)
    public SimplifiedStatsVO getSimplifiedCourseStats(Long courseId) {
        String cacheKey = "stats:course:" + courseId;
        String cachedData = redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            // ... 反序列化并返回（为简洁省略，假设有对应实现）
            // 这里先实现逻辑
        }

        List<AssignmentPO> assignments = assignmentRepository.selectList(
                new LambdaQueryWrapper<AssignmentPO>().eq(AssignmentPO::getCourseId, courseId)
        );
        List<Long> assignmentIds = assignments.stream().map(AssignmentPO::getId).collect(Collectors.toList());

        Double average = 0.0;
        Double passRate = 0.0;
        List<SimplifiedStatsVO.DistributionEntry> distribution = new ArrayList<>();

        if (!assignmentIds.isEmpty()) {
            List<AssignmentSubmissionPO> submissions = submissionRepository.selectList(
                    new LambdaQueryWrapper<AssignmentSubmissionPO>()
                            .in(AssignmentSubmissionPO::getAssignmentId, assignmentIds)
                            .isNotNull(AssignmentSubmissionPO::getScore)
            );

            if (!submissions.isEmpty()) {
                average = submissions.stream().mapToDouble(AssignmentSubmissionPO::getScore).average().orElse(0.0);
                long passedCount = submissions.stream().filter(s -> s.getScore() >= 60).count();
                passRate = (double) passedCount / submissions.size() * 100;

                distribution.add(SimplifiedStatsVO.DistributionEntry.builder().range("90-100").count(submissions.stream().filter(s -> s.getScore() >= 90).count()).build());
                distribution.add(SimplifiedStatsVO.DistributionEntry.builder().range("80-89").count(submissions.stream().filter(s -> s.getScore() >= 80 && s.getScore() < 90).count()).build());
                distribution.add(SimplifiedStatsVO.DistributionEntry.builder().range("70-79").count(submissions.stream().filter(s -> s.getScore() >= 70 && s.getScore() < 80).count()).build());
                distribution.add(SimplifiedStatsVO.DistributionEntry.builder().range("60-69").count(submissions.stream().filter(s -> s.getScore() >= 60 && s.getScore() < 70).count()).build());
                distribution.add(SimplifiedStatsVO.DistributionEntry.builder().range("<60").count(submissions.stream().filter(s -> s.getScore() < 60).count()).build());
            }
        }

        SimplifiedStatsVO stats = SimplifiedStatsVO.builder()
                .average(Math.round(average * 100.0) / 100.0)
                .passRate(Math.round(passRate * 100.0) / 100.0)
                .distribution(distribution)
                .build();

        // 缓存 4 小时
        // redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(stats), 4, TimeUnit.HOURS);

        return stats;
    }
}
