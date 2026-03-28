package com.coursebuddy.repository;

import com.coursebuddy.domain.po.CourseEnrollmentPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollmentPO, Long> {

    Optional<CourseEnrollmentPO> findByCourseIdAndUserId(Long courseId, Long userId);

    Page<CourseEnrollmentPO> findByUserId(Long userId, Pageable pageable);

    Page<CourseEnrollmentPO> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    Page<CourseEnrollmentPO> findByCourseId(Long courseId, Pageable pageable);

    Page<CourseEnrollmentPO> findByCourseIdAndStatus(Long courseId, String status, Pageable pageable);

    boolean existsByCourseIdAndUserId(Long courseId, Long userId);

    long countByCourseId(Long courseId);

    long countByUserId(Long userId);
}
