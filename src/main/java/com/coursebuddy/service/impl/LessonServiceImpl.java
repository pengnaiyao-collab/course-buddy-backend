package com.coursebuddy.service.impl;

import com.coursebuddy.domain.auth.Role;
import com.coursebuddy.domain.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.LessonDTO;
import com.coursebuddy.domain.po.LessonPO;
import com.coursebuddy.domain.vo.LessonVO;
import com.coursebuddy.converter.LessonConverter;
import com.coursebuddy.mapper.LessonMapper;
import com.coursebuddy.mapper.CourseCatalogMapper;
import com.coursebuddy.service.ILessonService;
import com.coursebuddy.util.AccessControlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课时服务实现
 */
@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements ILessonService {

    private final LessonMapper lessonRepository;
    private final LessonConverter lessonMapper;
    private final CourseCatalogMapper courseRepository;
    private final AccessControlValidator accessControlValidator;

    private void checkTeacherOrTA(User user) {
        if (user.getRole() != Role.TEACHER && user.getRole() != Role.TA) {
            throw new BusinessException(403, "Only teachers and TAs can perform this action");
        }
    }

    @Override
    @Transactional
    public LessonVO createLesson(Long courseId, LessonDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        checkTeacherOrTA(currentUser);
        // 验证用户是否是课程讲师
        accessControlValidator.validateCourseInstructor(courseId, currentUser.getId());
        
        LessonPO po = lessonMapper.dtoToPo(dto);
        po.setCourseId(courseId);
        if (po.getLessonOrder() == null) {
            int maxOrder = lessonRepository.findMaxLessonOrderByCourseId(courseId).orElse(0);
            po.setLessonOrder(maxOrder + 1);
        }
        lessonRepository.insert(po);
        return lessonMapper.poToVo(po);
    }

    @Override
    @Transactional
    public LessonVO updateLesson(Long id, LessonDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        checkTeacherOrTA(currentUser);
        LessonPO po = lessonRepository.selectById(id);
        if (po == null || po.getDeletedAt() != null) {
            throw new BusinessException(404, "Lesson not found");
        }
        // 验证用户是否是课程讲师
        accessControlValidator.validateCourseInstructor(po.getCourseId(), currentUser.getId());
        
        if (dto.getTitle() != null) po.setTitle(dto.getTitle());
        if (dto.getDescription() != null) po.setDescription(dto.getDescription());
        if (dto.getContent() != null) po.setContent(dto.getContent());
        if (dto.getLessonOrder() != null) po.setLessonOrder(dto.getLessonOrder());
        if (dto.getDuration() != null) po.setDuration(dto.getDuration());
        if (dto.getVideoUrl() != null) po.setVideoUrl(dto.getVideoUrl());
        if (dto.getResourceUrls() != null) po.setResourceUrls(dto.getResourceUrls());
        lessonRepository.updateById(po);
        return lessonMapper.poToVo(po);
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        checkTeacherOrTA(currentUser);
        LessonPO po = lessonRepository.selectById(id);
        if (po == null || po.getDeletedAt() != null) {
            throw new BusinessException(404, "Lesson not found");
        }
        // 验证用户是否是课程讲师
        accessControlValidator.validateCourseInstructor(po.getCourseId(), currentUser.getId());
        
        po.setDeletedAt(LocalDateTime.now());
        lessonRepository.updateById(po);
    }

    @Override
    @Transactional(readOnly = true)
    public LessonVO getLesson(Long id) {
        LessonPO po = lessonRepository.selectById(id);
        if (po == null || po.getDeletedAt() != null) {
            throw new BusinessException(404, "Lesson not found");
        }
        return lessonMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LessonVO> listLessons(Long courseId, Pageable pageable) {
        IPage<LessonPO> poPage = lessonRepository.findByCourseIdAndDeletedAtIsNullOrderByLessonOrderAsc(
                MybatisPlusPageUtils.toMpPage(pageable), courseId);
        return lessonMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional
    public LessonVO publishLesson(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        checkTeacherOrTA(currentUser);
        LessonPO po = lessonRepository.selectById(id);
        if (po == null || po.getDeletedAt() != null) {
            throw new BusinessException(404, "Lesson not found");
        }
        // 验证用户是否是课程讲师
        accessControlValidator.validateCourseInstructor(po.getCourseId(), currentUser.getId());
        
        po.setIsPublished(true);
        lessonRepository.updateById(po);
        return lessonMapper.poToVo(po);
    }

    @Override
    @Transactional
    public void reorderLessons(Long courseId, List<Long> lessonIds) {
        User currentUser = SecurityUtils.getCurrentUser();
        checkTeacherOrTA(currentUser);
        // 验证用户是否是课程讲师
        accessControlValidator.validateCourseInstructor(courseId, currentUser.getId());
        
        for (int i = 0; i < lessonIds.size(); i++) {
            Long lessonId = lessonIds.get(i);
            LessonPO po = lessonRepository.selectById(lessonId);
            if (po == null || po.getDeletedAt() != null) {
                throw new BusinessException(404, "Lesson not found: " + lessonId);
            }
            po.setLessonOrder(i + 1);
            lessonRepository.updateById(po);
        }
    }
}
