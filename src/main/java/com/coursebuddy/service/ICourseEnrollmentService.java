package com.coursebuddy.service;

import com.coursebuddy.domain.dto.CourseEnrollmentDTO;
import com.coursebuddy.domain.vo.CourseEnrollmentVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICourseEnrollmentService {
    CourseEnrollmentVO enroll(CourseEnrollmentDTO dto);
    void drop(Long courseId);
    void complete(Long courseId);
    Page<CourseEnrollmentVO> listMyEnrollments(String status, Pageable pageable);
    Page<CourseEnrollmentVO> listCourseStudents(Long courseId, Pageable pageable);
    boolean isEnrolled(Long courseId);
    long countCourseStudents(Long courseId);
}
