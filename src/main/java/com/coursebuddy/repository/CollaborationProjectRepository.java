package com.coursebuddy.repository;

import com.coursebuddy.domain.po.CollaborationProjectPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollaborationProjectRepository extends JpaRepository<CollaborationProjectPO, Long> {

    Page<CollaborationProjectPO> findByOwnerId(Long ownerId, Pageable pageable);

    Page<CollaborationProjectPO> findByCourseId(Long courseId, Pageable pageable);
}
