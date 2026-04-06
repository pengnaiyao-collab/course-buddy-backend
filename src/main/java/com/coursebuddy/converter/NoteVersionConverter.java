package com.coursebuddy.converter;

import com.coursebuddy.domain.po.NoteVersionPO;
import com.coursebuddy.domain.vo.NoteVersionVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 笔记版本转换器
 */
@Component
public class NoteVersionConverter {

    public NoteVersionVO poToVo(NoteVersionPO po) {
        if (po == null) return null;
        return NoteVersionVO.builder()
                .id(po.getId())
                .noteId(po.getNoteId())
                .versionNo(po.getVersionNo())
                .title(po.getTitle())
                .content(po.getContent())
                .changedBy(po.getChangedBy())
                .changeDesc(po.getChangeDesc())
                .createdAt(po.getCreatedAt())
                .build();
    }

    public List<NoteVersionVO> poListToVoList(List<NoteVersionPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }
}
