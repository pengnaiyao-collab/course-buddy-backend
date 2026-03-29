package com.coursebuddy.converter;

import com.coursebuddy.domain.dto.AssignmentDTO;
import com.coursebuddy.domain.po.AssignmentPO;
import com.coursebuddy.domain.vo.AssignmentVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AssignmentConverter {

    public AssignmentPO dtoToPo(AssignmentDTO dto) {
        if (dto == null) return null;
        return AssignmentPO.builder()
                .courseId(dto.getCourseId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .maxScore(dto.getMaxScore() != null ? dto.getMaxScore() : 100)
                .attachmentUrl(dto.getAttachmentUrl())
                .build();
    }

    public AssignmentVO poToVo(AssignmentPO po) {
        if (po == null) return null;
        return AssignmentVO.builder()
                .id(po.getId())
                .courseId(po.getCourseId())
                .title(po.getTitle())
                .description(po.getDescription())
                .dueDate(po.getDueDate())
                .maxScore(po.getMaxScore())
                .attachmentUrl(po.getAttachmentUrl())
                .isPublished(po.getIsPublished())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<AssignmentVO> poListToVoList(List<AssignmentPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<AssignmentVO> poPageToVoPage(Page<AssignmentPO> page) {
        return page.map(this::poToVo);
    }
}
