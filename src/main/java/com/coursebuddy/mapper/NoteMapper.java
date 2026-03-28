package com.coursebuddy.mapper;

import com.coursebuddy.domain.dto.NoteDTO;
import com.coursebuddy.domain.po.NotePO;
import com.coursebuddy.domain.vo.NoteVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NoteMapper {

    public NotePO dtoToPo(NoteDTO dto) {
        if (dto == null) return null;
        return NotePO.builder()
                .courseId(dto.getCourseId())
                .categoryId(dto.getCategoryId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : "DRAFT")
                .category(dto.getCategory())
                .tags(dto.getTags())
                .isPublic(dto.getIsPublic() != null ? dto.getIsPublic() : false)
                .build();
    }

    public NoteVO poToVo(NotePO po) {
        if (po == null) return null;
        return NoteVO.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .courseId(po.getCourseId())
                .categoryId(po.getCategoryId())
                .title(po.getTitle())
                .content(po.getContent())
                .description(po.getDescription())
                .status(po.getStatus())
                .category(po.getCategory())
                .tags(po.getTags())
                .isPublic(po.getIsPublic())
                .version(po.getOptLockVersion())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<NoteVO> poListToVoList(List<NotePO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<NoteVO> poPageToVoPage(Page<NotePO> page) {
        return page.map(this::poToVo);
    }
}
