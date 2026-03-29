package com.coursebuddy.converter;

import com.coursebuddy.domain.po.NoteSharePO;
import com.coursebuddy.domain.vo.NoteShareVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NoteShareConverter {

    public NoteShareVO poToVo(NoteSharePO po) {
        if (po == null) return null;
        return NoteShareVO.builder()
                .id(po.getId())
                .noteId(po.getNoteId())
                .ownerId(po.getOwnerId())
                .shareToken(po.getShareToken())
                .permission(po.getPermission())
                .expiresAt(po.getExpiresAt())
                .accessCount(po.getAccessCount())
                .isActive(po.getIsActive())
                .createdAt(po.getCreatedAt())
                .build();
    }

    public List<NoteShareVO> poListToVoList(List<NoteSharePO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<NoteShareVO> poPageToVoPage(Page<NoteSharePO> page) {
        return page.map(this::poToVo);
    }
}
