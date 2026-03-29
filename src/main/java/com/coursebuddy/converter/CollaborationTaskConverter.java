package com.coursebuddy.converter;

import com.coursebuddy.domain.dto.CollaborationTaskDTO;
import com.coursebuddy.domain.po.CollaborationTaskPO;
import com.coursebuddy.domain.vo.CollaborationTaskVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CollaborationTaskConverter {

    public CollaborationTaskPO dtoToPo(CollaborationTaskDTO dto) {
        if (dto == null) return null;
        return CollaborationTaskPO.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .assigneeId(dto.getAssigneeId())
                .status(dto.getStatus())
                .priority(dto.getPriority())
                .dueDate(dto.getDueDate())
                .build();
    }

    public CollaborationTaskVO poToVo(CollaborationTaskPO po) {
        if (po == null) return null;
        return CollaborationTaskVO.builder()
                .id(po.getId())
                .projectId(po.getProjectId())
                .title(po.getTitle())
                .description(po.getDescription())
                .assigneeId(po.getAssigneeId())
                .creatorId(po.getCreatorId())
                .status(po.getStatus())
                .priority(po.getPriority())
                .dueDate(po.getDueDate())
                .completedAt(po.getCompletedAt())
                .progress(po.getProgress())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<CollaborationTaskVO> poListToVoList(List<CollaborationTaskPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<CollaborationTaskVO> poPageToVoPage(Page<CollaborationTaskPO> page) {
        return page.map(this::poToVo);
    }
}
