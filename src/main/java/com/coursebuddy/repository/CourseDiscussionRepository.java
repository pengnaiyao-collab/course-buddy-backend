package com.coursebuddy.repository;

import com.coursebuddy.domain.po.CourseDiscussionPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseDiscussionRepository extends JpaRepository<CourseDiscussionPO, Long> {

    Page<CourseDiscussionPO> findByCourseIdAndParentIdIsNullAndIsDeletedFalse(Long courseId, Pageable pageable);

    List<CourseDiscussionPO> findByParentIdAndIsDeletedFalseOrderByCreatedAtAsc(Long parentId);

    long countByCourseIdAndParentIdIsNullAndIsDeletedFalse(Long courseId);
}
