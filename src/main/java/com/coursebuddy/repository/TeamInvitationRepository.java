package com.coursebuddy.repository;

import com.coursebuddy.domain.po.TeamInvitationPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamInvitationRepository extends JpaRepository<TeamInvitationPO, Long> {

    List<TeamInvitationPO> findByInvitedUserIdAndStatus(Long invitedUserId, String status);

    Optional<TeamInvitationPO> findByTeamIdAndInvitedUserId(Long teamId, Long invitedUserId);

    boolean existsByTeamIdAndInvitedUserIdAndStatus(Long teamId, Long invitedUserId, String status);
}
