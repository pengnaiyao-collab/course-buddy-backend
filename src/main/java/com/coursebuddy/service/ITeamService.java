package com.coursebuddy.service;

import com.coursebuddy.domain.dto.TeamDTO;
import com.coursebuddy.domain.dto.TeamMemberDTO;
import com.coursebuddy.domain.vo.TeamMemberVO;
import com.coursebuddy.domain.vo.TeamVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ITeamService {
    TeamVO createTeam(TeamDTO dto);
    Page<TeamVO> listMyTeams(Pageable pageable);
    TeamVO getTeamById(Long id);
    TeamVO updateTeam(Long id, TeamDTO dto);
    void deleteTeam(Long id);
    TeamMemberVO addMember(Long teamId, TeamMemberDTO dto);
    void removeMember(Long teamId, Long userId);
    TeamMemberVO updateMemberRole(Long teamId, Long userId, String role);
    List<TeamMemberVO> listMembers(Long teamId);
}
