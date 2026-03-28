package com.coursebuddy.mapper;

import com.coursebuddy.domain.po.ProjectMemberPO;
import com.coursebuddy.domain.vo.ProjectMemberVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectMemberMapper {

    public ProjectMemberVO poToVo(ProjectMemberPO po) {
        if (po == null) return null;
        return ProjectMemberVO.builder()
                .id(po.getId())
                .projectId(po.getProjectId())
                .userId(po.getUserId())
                .role(po.getRole())
                .joinedAt(po.getJoinedAt())
                .createdAt(po.getCreatedAt())
                .build();
    }

    public List<ProjectMemberVO> poListToVoList(List<ProjectMemberPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<ProjectMemberVO> poPageToVoPage(Page<ProjectMemberPO> page) {
        return page.map(this::poToVo);
    }
}
