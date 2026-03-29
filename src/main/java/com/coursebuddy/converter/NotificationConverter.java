package com.coursebuddy.converter;

import com.coursebuddy.domain.dto.NotificationDTO;
import com.coursebuddy.domain.po.NotificationPO;
import com.coursebuddy.domain.vo.NotificationVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationConverter {

    public NotificationPO dtoToPo(NotificationDTO dto) {
        if (dto == null) return null;
        return NotificationPO.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .type(dto.getType() != null ? dto.getType() : "SYSTEM")
                .relatedId(dto.getRelatedId())
                .relatedType(dto.getRelatedType())
                .build();
    }

    public NotificationVO poToVo(NotificationPO po) {
        if (po == null) return null;
        return NotificationVO.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .title(po.getTitle())
                .content(po.getContent())
                .type(po.getType())
                .isRead(po.getIsRead())
                .relatedId(po.getRelatedId())
                .relatedType(po.getRelatedType())
                .expiresAt(po.getExpiresAt())
                .createdAt(po.getCreatedAt())
                .build();
    }

    public List<NotificationVO> poListToVoList(List<NotificationPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<NotificationVO> poPageToVoPage(Page<NotificationPO> page) {
        return page.map(this::poToVo);
    }
}
