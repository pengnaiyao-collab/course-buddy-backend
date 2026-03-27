package com.coursebuddy.mapper;

import com.coursebuddy.domain.dto.CollaborationProjectDTO;
import com.coursebuddy.domain.po.CollaborationProjectPO;
import com.coursebuddy.domain.vo.CollaborationProjectVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CollaborationProjectMapper {

    public CollaborationProjectPO dtoToPo(CollaborationProjectDTO dto) {
        if (dto == null) return null;
        return CollaborationProjectPO.builder()
                .courseId(dto.getCourseId())
                .name(dto.getName())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .build();
    }

    public CollaborationProjectVO poToVo(CollaborationProjectPO po) {
        if (po == null) return null;
        return CollaborationProjectVO.builder()
                .id(po.getId())
                .courseId(po.getCourseId())
                .name(po.getName())
                .description(po.getDescription())
                .ownerId(po.getOwnerId())
                .status(po.getStatus())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<CollaborationProjectVO> poListToVoList(List<CollaborationProjectPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<CollaborationProjectVO> poPageToVoPage(Page<CollaborationProjectPO> page) {
        return page.map(this::poToVo);
    }
}
