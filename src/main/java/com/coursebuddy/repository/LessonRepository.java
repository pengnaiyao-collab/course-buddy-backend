package com.coursebuddy.repository;

import com.coursebuddy.domain.po.LessonPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<LessonPO, Long> {

    Page<LessonPO> findByCourseIdAndDeletedAtIsNullOrderByLessonOrderAsc(Long courseId, Pageable pageable);

    List<LessonPO> findByCourseIdAndDeletedAtIsNullOrderByLessonOrderAsc(Long courseId);

    @Query("SELECT MAX(l.lessonOrder) FROM LessonPO l WHERE l.courseId = :courseId")
    Optional<Integer> findMaxLessonOrderByCourseId(@Param("courseId") Long courseId);

    long countByCourseIdAndDeletedAtIsNull(Long courseId);

    long countByCourseIdAndIsPublishedTrueAndDeletedAtIsNull(Long courseId);
}
