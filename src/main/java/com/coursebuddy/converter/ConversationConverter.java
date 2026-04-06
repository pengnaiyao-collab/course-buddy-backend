package com.coursebuddy.converter;

import com.coursebuddy.domain.po.ConversationMessagePO;
import com.coursebuddy.domain.po.ConversationPO;
import com.coursebuddy.domain.vo.ChatMessageVO;
import com.coursebuddy.domain.vo.ConversationVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话转换器
 */
@Component
public class ConversationConverter {

    public ConversationVO poToVo(ConversationPO po) {
        if (po == null) return null;
        return ConversationVO.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .title(po.getTitle())
                .model(po.getModel())
                .status(po.getStatus())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public Page<ConversationVO> poPageToVoPage(Page<ConversationPO> page) {
        return page.map(this::poToVo);
    }

    public ChatMessageVO messagePoToVo(ConversationMessagePO po) {
        if (po == null) return null;
        return ChatMessageVO.builder()
                .id(po.getId())
                .conversationId(po.getConversationId())
                .role(po.getRole())
                .content(po.getContent())
                .imageData(po.getImageData())
                .imageMimeType(po.getImageMimeType())
                .imageName(po.getImageName())
                .tokenCount(po.getTokenCount())
                .createdAt(po.getCreatedAt())
                .build();
    }

    public List<ChatMessageVO> messagePoListToVoList(List<ConversationMessagePO> list) {
        if (list == null) return null;
        return list.stream().map(this::messagePoToVo).collect(Collectors.toList());
    }
}
