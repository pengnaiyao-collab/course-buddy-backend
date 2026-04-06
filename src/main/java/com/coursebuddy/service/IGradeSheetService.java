package com.coursebuddy.service;

import com.coursebuddy.domain.dto.GradeUpdateDTO;
import com.coursebuddy.domain.vo.GradeSheetVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 成绩成绩单服务
 */
public interface IGradeSheetService {
    GradeSheetVO getOrCreateGradeSheet(Long courseId, Long studentId);
    GradeSheetVO updateGradeSheet(Long courseId, Long studentId, GradeUpdateDTO dto);
    GradeSheetVO getMyGrade(Long courseId);
    Page<GradeSheetVO> listCourseGrades(Long courseId, Pageable pageable);
}
