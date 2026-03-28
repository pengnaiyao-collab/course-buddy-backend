package com.coursebuddy.service;

import com.coursebuddy.domain.dto.ChatRequestDTO;
import com.coursebuddy.domain.vo.AiUsageStatsVO;
import com.coursebuddy.domain.vo.ChatMessageVO;
import com.coursebuddy.domain.vo.ChatResponseVO;
import com.coursebuddy.domain.vo.ConversationVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface IXunFeiAiService {

    /** 同步对话：返回完整回复 */
    ChatResponseVO chat(ChatRequestDTO dto);

    /** 流式对话：通过 SSE 实时推送回复 */
    SseEmitter chatStream(ChatRequestDTO dto);

    /** 获取当前用户的对话列表 */
    Page<ConversationVO> listConversations(Pageable pageable);

    /** 获取指定对话的消息历史 */
    List<ChatMessageVO> getConversationMessages(Long conversationId);

    /** 归档对话 */
    ConversationVO archiveConversation(Long conversationId);

    /** 删除对话 */
    void deleteConversation(Long conversationId);

    /** 获取当前用户的使用统计 */
    Page<AiUsageStatsVO> getUsageStats(Pageable pageable);
}
