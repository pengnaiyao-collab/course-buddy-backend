package com.coursebuddy.repository;

import com.coursebuddy.domain.po.LearningProgressPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningProgressRepository extends JpaRepository<LearningProgressPO, Long> {

    Optional<LearningProgressPO> findByUserIdAndCourseId(Long userId, Long courseId);

    Page<LearningProgressPO> findByUserId(Long userId, Pageable pageable);

    List<LearningProgressPO> findByCourseId(Long courseId);

    @Query("SELECT AVG(lp.progress) FROM LearningProgressPO lp WHERE lp.courseId = :courseId")
    Double getAverageProgressByCourseId(Long courseId);

    @Query("SELECT SUM(lp.studyMinutes) FROM LearningProgressPO lp WHERE lp.userId = :userId")
    Long getTotalStudyMinutesByUserId(Long userId);
}
