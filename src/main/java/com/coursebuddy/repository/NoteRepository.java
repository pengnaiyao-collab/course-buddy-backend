package com.coursebuddy.repository;

import com.coursebuddy.domain.po.NotePO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<NotePO, Long> {

    Page<NotePO> findByUserId(Long userId, Pageable pageable);

    Page<NotePO> findByUserIdAndCourseId(Long userId, Long courseId, Pageable pageable);

    Page<NotePO> findByUserIdAndTitleContainingIgnoreCase(Long userId, String keyword, Pageable pageable);
}
