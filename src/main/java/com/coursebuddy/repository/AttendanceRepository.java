package com.coursebuddy.repository;

import com.coursebuddy.domain.po.AttendancePO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendancePO, Long> {

    Page<AttendancePO> findByCourseIdAndStudentId(Long courseId, Long studentId, Pageable pageable);

    Page<AttendancePO> findByCourseId(Long courseId, Pageable pageable);

    List<AttendancePO> findByCourseIdAndSessionDate(Long courseId, LocalDate sessionDate);

    Optional<AttendancePO> findByCourseIdAndStudentIdAndSessionDate(Long courseId, Long studentId, LocalDate sessionDate);

    long countByCourseIdAndStudentId(Long courseId, Long studentId);

    long countByCourseIdAndStudentIdAndStatus(Long courseId, Long studentId, String status);
}
