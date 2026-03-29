package com.coursebuddy.converter;

import com.coursebuddy.domain.dto.LearningTaskDTO;
import com.coursebuddy.domain.po.LearningTaskPO;
import com.coursebuddy.domain.vo.LearningTaskVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LearningTaskConverter {

    public LearningTaskPO dtoToPo(LearningTaskDTO dto) {
        if (dto == null) return null;
        return LearningTaskPO.builder()
                .courseId(dto.getCourseId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .dueDate(dto.getDueDate())
                .priority(dto.getPriority())
                .build();
    }

    public LearningTaskVO poToVo(LearningTaskPO po) {
        if (po == null) return null;
        return LearningTaskVO.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .courseId(po.getCourseId())
                .title(po.getTitle())
                .description(po.getDescription())
                .status(po.getStatus())
                .dueDate(po.getDueDate())
                .priority(po.getPriority())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<LearningTaskVO> poListToVoList(List<LearningTaskPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<LearningTaskVO> poPageToVoPage(Page<LearningTaskPO> page) {
        return page.map(this::poToVo);
    }
}
