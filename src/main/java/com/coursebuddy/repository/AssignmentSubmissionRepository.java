package com.coursebuddy.repository;

import com.coursebuddy.domain.po.AssignmentSubmissionPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmissionPO, Long> {

    Page<AssignmentSubmissionPO> findByAssignmentId(Long assignmentId, Pageable pageable);

    Optional<AssignmentSubmissionPO> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    long countByAssignmentId(Long assignmentId);

    long countByAssignmentIdAndStatus(Long assignmentId, String status);

    @Query("SELECT AVG(s.score) FROM AssignmentSubmissionPO s WHERE s.assignmentId = :assignmentId AND s.score IS NOT NULL")
    Double findAverageScoreByAssignmentId(@Param("assignmentId") Long assignmentId);
}
