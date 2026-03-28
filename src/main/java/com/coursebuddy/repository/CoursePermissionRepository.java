package com.coursebuddy.repository;

import com.coursebuddy.domain.po.CoursePermissionPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoursePermissionRepository extends JpaRepository<CoursePermissionPO, Long> {

    Optional<CoursePermissionPO> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CoursePermissionPO> findByCourseId(Long courseId);

    List<CoursePermissionPO> findByUserId(Long userId);

    List<CoursePermissionPO> findByCourseIdAndPermissionLevel(Long courseId, String permissionLevel);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    void deleteByUserIdAndCourseId(Long userId, Long courseId);
}
