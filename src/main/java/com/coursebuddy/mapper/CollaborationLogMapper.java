package com.coursebuddy.mapper;

import com.coursebuddy.domain.po.CollaborationLogPO;
import com.coursebuddy.domain.vo.CollaborationLogVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CollaborationLogMapper {

    public CollaborationLogVO poToVo(CollaborationLogPO po) {
        if (po == null) return null;
        return CollaborationLogVO.builder()
                .id(po.getId())
                .projectId(po.getProjectId())
                .userId(po.getUserId())
                .actionType(po.getActionType())
                .entityType(po.getEntityType())
                .entityId(po.getEntityId())
                .changeData(po.getChangeData())
                .createdAt(po.getCreatedAt())
                .build();
    }

    public List<CollaborationLogVO> poListToVoList(List<CollaborationLogPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<CollaborationLogVO> poPageToVoPage(Page<CollaborationLogPO> page) {
        return page.map(this::poToVo);
    }
}
