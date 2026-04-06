package com.coursebuddy.service;

import com.coursebuddy.domain.dto.LearningProgressDTO;
import com.coursebuddy.domain.vo.LearningProgressVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 进度服务
 */
public interface ILearningProgressService {
    LearningProgressVO updateProgress(LearningProgressDTO dto);
    LearningProgressVO getMyProgressForCourse(Long courseId);
    Page<LearningProgressVO> listMyProgress(Pageable pageable);
    Map<String, Object> getMyCourseStats(Long courseId);
    Double getAverageCourseProgress(Long courseId);
}
