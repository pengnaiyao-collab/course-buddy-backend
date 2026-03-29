package com.coursebuddy.repository;

import com.coursebuddy.domain.po.CourseResourcePO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseResourceRepository extends JpaRepository<CourseResourcePO, Long> {

    Page<CourseResourcePO> findByCourseId(Long courseId, Pageable pageable);

    @Modifying
    @Query("UPDATE CourseResourcePO r SET r.downloadCount = r.downloadCount + 1 WHERE r.id = :id")
    void incrementDownloadCount(@Param("id") Long id);
}
