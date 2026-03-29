package com.coursebuddy.service;

import com.coursebuddy.domain.dto.CourseDTO;
import com.coursebuddy.domain.vo.CourseStatsVO;
import com.coursebuddy.domain.vo.CourseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICourseService {
    CourseVO createCourse(CourseDTO dto);
    CourseVO updateCourse(Long id, CourseDTO dto);
    void deleteCourse(Long id);
    CourseVO getCourse(Long id);
    Page<CourseVO> listAllCourses(Pageable pageable);
    Page<CourseVO> listMyTeachingCourses(Pageable pageable);
    Page<CourseVO> searchCourses(String keyword, String level, Pageable pageable);
    CourseStatsVO getCourseStats(Long courseId);
}
