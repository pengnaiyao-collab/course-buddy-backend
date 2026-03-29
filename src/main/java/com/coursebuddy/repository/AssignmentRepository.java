package com.coursebuddy.repository;

import com.coursebuddy.domain.po.AssignmentPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<AssignmentPO, Long> {

    Page<AssignmentPO> findByCourseIdAndDeletedAtIsNull(Long courseId, Pageable pageable);

    List<AssignmentPO> findByCourseIdAndDeletedAtIsNull(Long courseId);

    long countByCourseIdAndDeletedAtIsNull(Long courseId);
}
