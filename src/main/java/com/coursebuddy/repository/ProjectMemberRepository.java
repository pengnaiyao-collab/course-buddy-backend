package com.coursebuddy.repository;

import com.coursebuddy.domain.po.ProjectMemberPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMemberPO, Long> {

    List<ProjectMemberPO> findByProjectId(Long projectId);

    Page<ProjectMemberPO> findByProjectId(Long projectId, Pageable pageable);

    Optional<ProjectMemberPO> findByProjectIdAndUserId(Long projectId, Long userId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    void deleteByProjectIdAndUserId(Long projectId, Long userId);

    long countByProjectId(Long projectId);
}
