package com.coursebuddy.converter;

import com.coursebuddy.domain.dto.NoteCategoryDTO;
import com.coursebuddy.domain.po.NoteCategoryPO;
import com.coursebuddy.domain.vo.NoteCategoryVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 笔记分类转换器
 */
@Component
public class NoteCategoryConverter {

    public NoteCategoryPO dtoToPo(NoteCategoryDTO dto) {
        if (dto == null) return null;
        return NoteCategoryPO.builder()
                .name(dto.getName())
                .color(dto.getColor())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .build();
    }

    public NoteCategoryVO poToVo(NoteCategoryPO po) {
        if (po == null) return null;
        return NoteCategoryVO.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .name(po.getName())
                .description(po.getDescription())
                .color(po.getColor())
                .sortOrder(po.getSortOrder())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<NoteCategoryVO> poListToVoList(List<NoteCategoryPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }
}
