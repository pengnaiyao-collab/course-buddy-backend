package com.coursebuddy.repository;

import com.coursebuddy.domain.po.NotePO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<NotePO, Long> {

    Page<NotePO> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Page<NotePO> findByUserIdAndCourseIdAndIsDeletedFalse(Long userId, Long courseId, Pageable pageable);

    Page<NotePO> findByUserIdAndCategoryIdAndIsDeletedFalse(Long userId, Long categoryId, Pageable pageable);

    Page<NotePO> findByUserIdAndTitleContainingIgnoreCaseAndIsDeletedFalse(Long userId, String keyword, Pageable pageable);

    Page<NotePO> findByUserIdAndIdInAndIsDeletedFalse(Long userId, List<Long> ids, Pageable pageable);
}
