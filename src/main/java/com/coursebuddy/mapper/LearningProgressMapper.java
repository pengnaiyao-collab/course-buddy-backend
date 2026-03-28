package com.coursebuddy.mapper;

import com.coursebuddy.domain.dto.LearningProgressDTO;
import com.coursebuddy.domain.po.LearningProgressPO;
import com.coursebuddy.domain.vo.LearningProgressVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LearningProgressMapper {

    public LearningProgressPO dtoToPo(LearningProgressDTO dto) {
        if (dto == null) return null;
        return LearningProgressPO.builder()
                .courseId(dto.getCourseId())
                .resourceId(dto.getResourceId())
                .progress(dto.getProgress() != null ? dto.getProgress() : 0)
                .studyMinutes(dto.getStudyMinutes() != null ? dto.getStudyMinutes() : 0)
                .notes(dto.getNotes())
                .build();
    }

    public LearningProgressVO poToVo(LearningProgressPO po) {
        if (po == null) return null;
        return LearningProgressVO.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .courseId(po.getCourseId())
                .resourceId(po.getResourceId())
                .progress(po.getProgress())
                .studyMinutes(po.getStudyMinutes())
                .lastStudiedAt(po.getLastStudiedAt())
                .notes(po.getNotes())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<LearningProgressVO> poListToVoList(List<LearningProgressPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<LearningProgressVO> poPageToVoPage(Page<LearningProgressPO> page) {
        return page.map(this::poToVo);
    }
}
