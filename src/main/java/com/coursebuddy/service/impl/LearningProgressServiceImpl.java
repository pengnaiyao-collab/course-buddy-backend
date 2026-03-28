package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.LearningProgressDTO;
import com.coursebuddy.domain.po.LearningProgressPO;
import com.coursebuddy.domain.vo.LearningProgressVO;
import com.coursebuddy.mapper.LearningProgressMapper;
import com.coursebuddy.repository.LearningProgressRepository;
import com.coursebuddy.service.ILearningProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LearningProgressServiceImpl implements ILearningProgressService {

    private final LearningProgressRepository progressRepository;
    private final LearningProgressMapper progressMapper;

    @Override
    @Transactional
    public LearningProgressVO updateProgress(LearningProgressDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        LearningProgressPO po = progressRepository
                .findByUserIdAndCourseId(currentUser.getId(), dto.getCourseId())
                .orElseGet(() -> LearningProgressPO.builder()
                        .userId(currentUser.getId())
                        .courseId(dto.getCourseId())
                        .build());
        if (dto.getResourceId() != null) po.setResourceId(dto.getResourceId());
        if (dto.getProgress() != null) po.setProgress(dto.getProgress());
        if (dto.getStudyMinutes() != null) {
            int current = po.getStudyMinutes() != null ? po.getStudyMinutes() : 0;
            po.setStudyMinutes(current + dto.getStudyMinutes());
        }
        if (dto.getNotes() != null) po.setNotes(dto.getNotes());
        po.setLastStudiedAt(LocalDateTime.now());
        return progressMapper.poToVo(progressRepository.save(po));
    }

    @Override
    @Transactional(readOnly = true)
    public LearningProgressVO getMyProgressForCourse(Long courseId) {
        User currentUser = SecurityUtils.getCurrentUser();
        LearningProgressPO po = progressRepository
                .findByUserIdAndCourseId(currentUser.getId(), courseId)
                .orElseThrow(() -> new BusinessException(404, "Learning progress not found for this course"));
        return progressMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LearningProgressVO> listMyProgress(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        return progressMapper.poPageToVoPage(
                progressRepository.findByUserId(currentUser.getId(), pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMyCourseStats(Long courseId) {
        User currentUser = SecurityUtils.getCurrentUser();
        Long totalMinutes = progressRepository.getTotalStudyMinutesByUserId(currentUser.getId());
        LearningProgressPO po = progressRepository
                .findByUserIdAndCourseId(currentUser.getId(), courseId)
                .orElse(null);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudyMinutes", totalMinutes != null ? totalMinutes : 0);
        stats.put("courseProgress", po != null ? po.getProgress() : 0);
        stats.put("courseStudyMinutes", po != null ? po.getStudyMinutes() : 0);
        stats.put("lastStudiedAt", po != null ? po.getLastStudiedAt() : null);
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageCourseProgress(Long courseId) {
        Double avg = progressRepository.getAverageProgressByCourseId(courseId);
        return avg != null ? avg : 0.0;
    }
}
