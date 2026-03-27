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
                .title(dto.getTitle())
                .content(dto.getContent())
                .category(dto.getCategory())
                .tags(dto.getTags())
                .build();
    }

    public NoteVO poToVo(NotePO po) {
        if (po == null) return null;
        return NoteVO.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .courseId(po.getCourseId())
                .title(po.getTitle())
                .content(po.getContent())
                .category(po.getCategory())
                .tags(po.getTags())
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
