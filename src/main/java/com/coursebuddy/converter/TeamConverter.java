package com.coursebuddy.converter;

import com.coursebuddy.domain.dto.TeamDTO;
import com.coursebuddy.domain.po.TeamMemberPO;
import com.coursebuddy.domain.po.TeamPO;
import com.coursebuddy.domain.vo.TeamMemberVO;
import com.coursebuddy.domain.vo.TeamVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TeamConverter {

    public TeamPO dtoToPo(TeamDTO dto) {
        if (dto == null) return null;
        return TeamPO.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .avatarUrl(dto.getAvatarUrl())
                .courseId(dto.getCourseId())
                .projectId(dto.getProjectId())
                .build();
    }

    public TeamVO poToVo(TeamPO po) {
        if (po == null) return null;
        return TeamVO.builder()
                .id(po.getId())
                .name(po.getName())
                .description(po.getDescription())
                .avatarUrl(po.getAvatarUrl())
                .ownerId(po.getOwnerId())
                .courseId(po.getCourseId())
                .projectId(po.getProjectId())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public TeamMemberVO memberPoToVo(TeamMemberPO po) {
        if (po == null) return null;
        return TeamMemberVO.builder()
                .id(po.getId())
                .teamId(po.getTeamId())
                .userId(po.getUserId())
                .role(po.getRole())
                .joinedAt(po.getJoinedAt())
                .build();
    }

    public List<TeamVO> poListToVoList(List<TeamPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<TeamVO> poPageToVoPage(Page<TeamPO> page) {
        return page.map(this::poToVo);
    }

    public List<TeamMemberVO> memberPoListToVoList(List<TeamMemberPO> list) {
        if (list == null) return null;
        return list.stream().map(this::memberPoToVo).collect(Collectors.toList());
    }
}
