package com.coursebuddy.service;

import com.coursebuddy.domain.dto.ChatRequestDTO;
import com.coursebuddy.domain.vo.ChatMessageVO;
import com.coursebuddy.domain.vo.ChatResponseVO;
import com.coursebuddy.domain.vo.ConversationVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 聊天服务
 */
public interface IAIChatService {

    ChatResponseVO chat(ChatRequestDTO dto);

    SseEmitter chatStream(ChatRequestDTO dto);

    Page<ConversationVO> listConversations(Pageable pageable);

    List<ChatMessageVO> getConversationMessages(Long conversationId);

    ConversationVO archiveConversation(Long conversationId);

    void deleteConversation(Long conversationId);
}
