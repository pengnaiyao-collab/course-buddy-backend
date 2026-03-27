package com.coursebuddy.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    Page<Course> findByTeacherId(Long teacherId, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.status = com.coursebuddy.course.CourseStatus.PUBLISHED AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.category) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Course> searchPublished(@Param("keyword") String keyword, Pageable pageable);
}
