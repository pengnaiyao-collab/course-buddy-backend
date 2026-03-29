package com.coursebuddy.repository;

import com.coursebuddy.domain.po.GradeSheetPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeSheetRepository extends JpaRepository<GradeSheetPO, Long> {

    Optional<GradeSheetPO> findByCourseIdAndStudentId(Long courseId, Long studentId);

    Page<GradeSheetPO> findByCourseId(Long courseId, Pageable pageable);

    List<GradeSheetPO> findByCourseId(Long courseId);
}
