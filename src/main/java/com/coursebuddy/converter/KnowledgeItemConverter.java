package com.coursebuddy.converter;

import com.coursebuddy.domain.dto.KnowledgeItemDTO;
import com.coursebuddy.domain.po.KnowledgeItemPO;
import com.coursebuddy.domain.vo.KnowledgeItemVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KnowledgeItemConverter {

    public KnowledgeItemPO dtoToPo(KnowledgeItemDTO dto) {
        if (dto == null) return null;
        return KnowledgeItemPO.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .content(dto.getContent())
                .fileUrl(dto.getFileUrl())
                .fileType(dto.getFileType())
                .category(dto.getCategory())
                .tags(dto.getTags())
                .sourceType(dto.getSourceType())
                .status(dto.getStatus())
                .build();
    }

    public KnowledgeItemVO poToVo(KnowledgeItemPO po) {
        if (po == null) return null;
        return KnowledgeItemVO.builder()
                .id(po.getId())
                .courseId(po.getCourseId())
                .title(po.getTitle())
                .description(po.getDescription())
                .content(po.getContent())
                .fileUrl(po.getFileUrl())
                .fileType(po.getFileType())
                .category(po.getCategory())
                .tags(po.getTags())
                .extractedText(po.getExtractedText())
                .sourceType(po.getSourceType())
                .status(po.getStatus())
                .createdBy(po.getCreatedBy())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<KnowledgeItemVO> poListToVoList(List<KnowledgeItemPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<KnowledgeItemVO> poPageToVoPage(Page<KnowledgeItemPO> page) {
        return page.map(this::poToVo);
    }
}
