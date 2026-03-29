package com.coursebuddy.repository;

import com.coursebuddy.domain.po.CoursePO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseCatalogRepository extends JpaRepository<CoursePO, Long> {

    Optional<CoursePO> findByCode(String code);

    Page<CoursePO> findByDeletedAtIsNull(Pageable pageable);

    Page<CoursePO> findByInstructorIdAndDeletedAtIsNull(Long instructorId, Pageable pageable);

    Page<CoursePO> findByStatusAndDeletedAtIsNull(String status, Pageable pageable);

    @Query("SELECT c FROM CoursePO c WHERE c.deletedAt IS NULL " +
           "AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:level IS NULL OR c.level = :level)")
    Page<CoursePO> searchCourses(@Param("keyword") String keyword, @Param("level") String level, Pageable pageable);

    @Modifying
    @Query("UPDATE CoursePO c SET c.enrolledCount = c.enrolledCount + 1 WHERE c.id = :id")
    void incrementEnrolledCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE CoursePO c SET c.enrolledCount = c.enrolledCount - 1 WHERE c.id = :id AND c.enrolledCount > 0")
    void decrementEnrolledCount(@Param("id") Long id);
}
