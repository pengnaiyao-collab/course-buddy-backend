package com.coursebuddy.service;

import com.coursebuddy.domain.dto.LessonDTO;
import com.coursebuddy.domain.vo.LessonVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ILessonService {
    LessonVO createLesson(Long courseId, LessonDTO dto);
    LessonVO updateLesson(Long id, LessonDTO dto);
    void deleteLesson(Long id);
    LessonVO getLesson(Long id);
    Page<LessonVO> listLessons(Long courseId, Pageable pageable);
    LessonVO publishLesson(Long id);
    void reorderLessons(Long courseId, List<Long> lessonIds);
}
