package com.coursebuddy.repository;

import com.coursebuddy.domain.po.TeamMemberPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMemberPO, Long> {

    List<TeamMemberPO> findByTeamId(Long teamId);

    Optional<TeamMemberPO> findByTeamIdAndUserId(Long teamId, Long userId);

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    void deleteByTeamIdAndUserId(Long teamId, Long userId);

    long countByTeamId(Long teamId);
}
