package com.coursebuddy.converter;

import com.coursebuddy.domain.dto.MessageDTO;
import com.coursebuddy.domain.po.MessagePO;
import com.coursebuddy.domain.vo.MessageVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MessageConverter {

    public MessagePO dtoToPo(MessageDTO dto) {
        if (dto == null) return null;
        return MessagePO.builder()
                .receiverId(dto.getReceiverId())
                .content(dto.getContent())
                .msgType(dto.getMsgType() != null ? dto.getMsgType() : "TEXT")
                .build();
    }

    public MessageVO poToVo(MessagePO po) {
        if (po == null) return null;
        return MessageVO.builder()
                .id(po.getId())
                .senderId(po.getSenderId())
                .receiverId(po.getReceiverId())
                .content(po.getContent())
                .msgType(po.getMsgType())
                .isRead(po.getIsRead())
                .createdAt(po.getCreatedAt())
                .build();
    }

    public List<MessageVO> poListToVoList(List<MessagePO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<MessageVO> poPageToVoPage(Page<MessagePO> page) {
        return page.map(this::poToVo);
    }
}
