package com.coursebuddy.mapper;

import com.coursebuddy.domain.po.TeamInvitationPO;
import com.coursebuddy.domain.vo.TeamInvitationVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TeamInvitationMapper {

    public TeamInvitationVO poToVo(TeamInvitationPO po) {
        if (po == null) return null;
        return TeamInvitationVO.builder()
                .id(po.getId())
                .teamId(po.getTeamId())
                .invitedUserId(po.getInvitedUserId())
                .invitedBy(po.getInvitedBy())
                .role(po.getRole())
                .status(po.getStatus())
                .expiredAt(po.getExpiredAt())
                .createdAt(po.getCreatedAt())
                .build();
    }

    public List<TeamInvitationVO> poListToVoList(List<TeamInvitationPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }
}
