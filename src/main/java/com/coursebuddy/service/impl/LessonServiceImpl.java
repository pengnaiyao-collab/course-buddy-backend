package com.coursebuddy.service.impl;

import com.coursebuddy.auth.Role;
import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.LessonDTO;
import com.coursebuddy.domain.po.LessonPO;
import com.coursebuddy.domain.vo.LessonVO;
import com.coursebuddy.mapper.LessonMapper;
import com.coursebuddy.repository.LessonRepository;
import com.coursebuddy.service.ILessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements ILessonService {

    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;

    private void checkTeacherOrAdmin(User user) {
        if (user.getRole() != Role.TEACHER && user.getRole() != Role.ADMIN) {
            throw new BusinessException(403, "Only teachers and admins can perform this action");
        }
    }

    @Override
    @Transactional
    public LessonVO createLesson(Long courseId, LessonDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        checkTeacherOrAdmin(currentUser);
        LessonPO po = lessonMapper.dtoToPo(dto);
        po.setCourseId(courseId);
        if (po.getLessonOrder() == null || po.getLessonOrder() == 1) {
            int maxOrder = lessonRepository.findMaxLessonOrderByCourseId(courseId).orElse(0);
            po.setLessonOrder(maxOrder + 1);
        }
        return lessonMapper.poToVo(lessonRepository.save(po));
    }

    @Override
    @Transactional
    public LessonVO updateLesson(Long id, LessonDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        checkTeacherOrAdmin(currentUser);
        LessonPO po = lessonRepository.findById(id)
                .filter(l -> l.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(404, "Lesson not found"));
        if (dto.getTitle() != null) po.setTitle(dto.getTitle());
        if (dto.getDescription() != null) po.setDescription(dto.getDescription());
        if (dto.getContent() != null) po.setContent(dto.getContent());
        if (dto.getLessonOrder() != null) po.setLessonOrder(dto.getLessonOrder());
        if (dto.getDuration() != null) po.setDuration(dto.getDuration());
        if (dto.getVideoUrl() != null) po.setVideoUrl(dto.getVideoUrl());
        if (dto.getResourceUrls() != null) po.setResourceUrls(dto.getResourceUrls());
        return lessonMapper.poToVo(lessonRepository.save(po));
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        checkTeacherOrAdmin(currentUser);
        LessonPO po = lessonRepository.findById(id)
                .filter(l -> l.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(404, "Lesson not found"));
        po.setDeletedAt(LocalDateTime.now());
        lessonRepository.save(po);
    }

    @Override
    @Transactional(readOnly = true)
    public LessonVO getLesson(Long id) {
        LessonPO po = lessonRepository.findById(id)
                .filter(l -> l.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(404, "Lesson not found"));
        return lessonMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LessonVO> listLessons(Long courseId, Pageable pageable) {
        return lessonMapper.poPageToVoPage(
                lessonRepository.findByCourseIdAndDeletedAtIsNullOrderByLessonOrderAsc(courseId, pageable));
    }

    @Override
    @Transactional
    public LessonVO publishLesson(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        checkTeacherOrAdmin(currentUser);
        LessonPO po = lessonRepository.findById(id)
                .filter(l -> l.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(404, "Lesson not found"));
        po.setIsPublished(true);
        return lessonMapper.poToVo(lessonRepository.save(po));
    }

    @Override
    @Transactional
    public void reorderLessons(Long courseId, List<Long> lessonIds) {
        User currentUser = SecurityUtils.getCurrentUser();
        checkTeacherOrAdmin(currentUser);
        for (int i = 0; i < lessonIds.size(); i++) {
            Long lessonId = lessonIds.get(i);
            LessonPO po = lessonRepository.findById(lessonId)
                    .filter(l -> l.getDeletedAt() == null)
                    .orElseThrow(() -> new BusinessException(404, "Lesson not found: " + lessonId));
            po.setLessonOrder(i + 1);
            lessonRepository.save(po);
        }
    }
}
