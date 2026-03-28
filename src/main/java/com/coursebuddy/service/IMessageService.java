package com.coursebuddy.service;

import com.coursebuddy.domain.dto.MessageDTO;
import com.coursebuddy.domain.vo.MessageVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IMessageService {
    MessageVO sendMessage(MessageDTO dto);
    Page<MessageVO> getConversation(Long otherUserId, Pageable pageable);
    Page<MessageVO> listInbox(Pageable pageable);
    int markConversationRead(Long senderId);
    long countUnread();
    void deleteMessage(Long id);
}
