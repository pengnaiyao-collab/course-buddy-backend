package com.coursebuddy.converter;

import com.coursebuddy.domain.dto.NoteTagDTO;
import com.coursebuddy.domain.po.NoteTagPO;
import com.coursebuddy.domain.vo.NoteTagVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NoteTagConverter {

    public NoteTagPO dtoToPo(NoteTagDTO dto) {
        if (dto == null) return null;
        return NoteTagPO.builder()
                .name(dto.getName())
                .color(dto.getColor())
                .build();
    }

    public NoteTagVO poToVo(NoteTagPO po) {
        if (po == null) return null;
        return NoteTagVO.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .name(po.getName())
                .color(po.getColor())
                .useCount(po.getUseCount())
                .createdAt(po.getCreatedAt())
                .build();
    }

    public List<NoteTagVO> poListToVoList(List<NoteTagPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }
}
