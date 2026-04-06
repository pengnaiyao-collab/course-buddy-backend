package com.coursebuddy.service;

import com.coursebuddy.domain.dto.CourseEnrollmentDTO;
import com.coursebuddy.domain.vo.CourseEnrollmentVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 课程选课服务
 */
public interface ICourseEnrollmentService {
    CourseEnrollmentVO enroll(CourseEnrollmentDTO dto);
    CourseEnrollmentVO enrollByCode(String code);
    void drop(Long courseId);
    void complete(Long courseId);
    Page<CourseEnrollmentVO> listMyEnrollments(String status, Pageable pageable);
    Page<CourseEnrollmentVO> listCourseStudents(Long courseId, Pageable pageable);
    boolean isEnrolled(Long courseId);
    long countCourseStudents(Long courseId);
}
