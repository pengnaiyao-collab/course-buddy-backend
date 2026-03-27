package com.coursebuddy.collaboration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollaborationProjectRepository extends JpaRepository<CollaborationProject, Long> {

    Page<CollaborationProject> findByOwnerId(Long ownerId, Pageable pageable);

    Page<CollaborationProject> findByCourseId(Long courseId, Pageable pageable);
}
