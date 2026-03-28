package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.TeamDTO;
import com.coursebuddy.domain.dto.TeamMemberDTO;
import com.coursebuddy.domain.po.TeamMemberPO;
import com.coursebuddy.domain.po.TeamPO;
import com.coursebuddy.domain.vo.TeamMemberVO;
import com.coursebuddy.domain.vo.TeamVO;
import com.coursebuddy.mapper.TeamMapper;
import com.coursebuddy.repository.TeamMemberRepository;
import com.coursebuddy.repository.TeamRepository;
import com.coursebuddy.service.ITeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements ITeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamMapper teamMapper;

    @Override
    @Transactional
    public TeamVO createTeam(TeamDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        TeamPO team = teamMapper.dtoToPo(dto);
        team.setOwnerId(currentUser.getId());
        TeamPO saved = teamRepository.save(team);
        TeamMemberPO ownerMember = TeamMemberPO.builder()
                .teamId(saved.getId())
                .userId(currentUser.getId())
                .role("OWNER")
                .build();
        teamMemberRepository.save(ownerMember);
        TeamVO vo = teamMapper.poToVo(saved);
        vo.setMemberCount(1);
        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeamVO> listMyTeams(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        return teamMapper.poPageToVoPage(
                teamRepository.findByMemberId(currentUser.getId(), pageable))
                .map(vo -> {
                    vo.setMemberCount((int) teamMemberRepository.countByTeamId(vo.getId()));
                    return vo;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public TeamVO getTeamById(Long id) {
        TeamPO po = teamRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Team not found"));
        TeamVO vo = teamMapper.poToVo(po);
        List<TeamMemberVO> members = teamMapper.memberPoListToVoList(
                teamMemberRepository.findByTeamId(id));
        vo.setMembers(members);
        vo.setMemberCount(members.size());
        return vo;
    }

    @Override
    @Transactional
    public TeamVO updateTeam(Long id, TeamDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        TeamPO po = teamRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Team not found"));
        if (!po.getOwnerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Only the team owner can update the team");
        }
        if (dto.getName() != null) po.setName(dto.getName());
        if (dto.getDescription() != null) po.setDescription(dto.getDescription());
        if (dto.getAvatarUrl() != null) po.setAvatarUrl(dto.getAvatarUrl());
        return teamMapper.poToVo(teamRepository.save(po));
    }

    @Override
    @Transactional
    public void deleteTeam(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        TeamPO po = teamRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Team not found"));
        if (!po.getOwnerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Only the team owner can delete the team");
        }
        teamRepository.delete(po);
    }

    @Override
    @Transactional
    public TeamMemberVO addMember(Long teamId, TeamMemberDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        TeamPO team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(404, "Team not found"));
        if (!team.getOwnerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Only the team owner can add members");
        }
        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, dto.getUserId())) {
            throw new BusinessException(409, "User is already a member of this team");
        }
        TeamMemberPO member = TeamMemberPO.builder()
                .teamId(teamId)
                .userId(dto.getUserId())
                .role(dto.getRole() != null ? dto.getRole() : "MEMBER")
                .build();
        return teamMapper.memberPoToVo(teamMemberRepository.save(member));
    }

    @Override
    @Transactional
    public void removeMember(Long teamId, Long userId) {
        User currentUser = SecurityUtils.getCurrentUser();
        TeamPO team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(404, "Team not found"));
        if (!team.getOwnerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Only the team owner can remove members");
        }
        teamMemberRepository.deleteByTeamIdAndUserId(teamId, userId);
    }

    @Override
    @Transactional
    public TeamMemberVO updateMemberRole(Long teamId, Long userId, String role) {
        User currentUser = SecurityUtils.getCurrentUser();
        TeamPO team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(404, "Team not found"));
        if (!team.getOwnerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Only the team owner can update member roles");
        }
        TeamMemberPO member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new BusinessException(404, "Member not found"));
        member.setRole(role);
        return teamMapper.memberPoToVo(teamMemberRepository.save(member));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberVO> listMembers(Long teamId) {
        return teamMapper.memberPoListToVoList(teamMemberRepository.findByTeamId(teamId));
    }
}
