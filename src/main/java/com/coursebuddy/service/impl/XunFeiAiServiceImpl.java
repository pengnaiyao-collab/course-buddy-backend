package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.client.XunFeiSparkClient;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.config.XunFeiProperties;
import com.coursebuddy.domain.dto.ChatRequestDTO;
import com.coursebuddy.domain.po.AiUsageStatsPO;
import com.coursebuddy.domain.po.ConversationMessagePO;
import com.coursebuddy.domain.po.ConversationPO;
import com.coursebuddy.domain.vo.AiUsageStatsVO;
import com.coursebuddy.domain.vo.ChatMessageVO;
import com.coursebuddy.domain.vo.ChatResponseVO;
import com.coursebuddy.domain.vo.ConversationVO;
import com.coursebuddy.mapper.ConversationMapper;
import com.coursebuddy.repository.AiUsageStatsRepository;
import com.coursebuddy.repository.ConversationMessageRepository;
import com.coursebuddy.repository.ConversationRepository;
import com.coursebuddy.service.IXunFeiAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class XunFeiAiServiceImpl implements IXunFeiAiService {

    private final XunFeiSparkClient sparkClient;
    private final XunFeiProperties properties;
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository messageRepository;
    private final AiUsageStatsRepository usageStatsRepository;
    private final ConversationMapper conversationMapper;

    private static final int MAX_CONTEXT_MESSAGES = 20;
    private final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    @Override
    @Transactional
    public ChatResponseVO chat(ChatRequestDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        long startTime = System.currentTimeMillis();
        String requestType = "CHAT";

        ConversationPO conversation = resolveConversation(dto, currentUser.getId());

        List<Map<String, String>> messages = buildMessageContext(conversation.getId(), dto.getMessage());

        String answer;
        int[] tokens = {0, 0};
        try {
            answer = sparkClient.chat(messages, String.valueOf(currentUser.getId()));
        } catch (Exception e) {
            log.error("XunFei chat failed for user {}", currentUser.getId(), e);
            recordUsageStats(currentUser.getId(), requestType, 0, 0,
                    System.currentTimeMillis() - startTime, "FAILED", e.getMessage());
            throw new BusinessException(500, "AI 服务暂时不可用，请稍后重试");
        }

        // Save user message
        saveMessage(conversation.getId(), "user", dto.getMessage(), null);
        // Save assistant message
        saveMessage(conversation.getId(), "assistant", answer, null);

        // Update conversation title if new
        if (conversation.getTitle() == null || conversation.getTitle().isBlank()) {
            String title = dto.getMessage().length() > 30
                    ? dto.getMessage().substring(0, 30) + "..."
                    : dto.getMessage();
            conversation.setTitle(title);
            conversationRepository.save(conversation);
        }

        recordUsageStats(currentUser.getId(), requestType, tokens[0], tokens[1],
                System.currentTimeMillis() - startTime, "SUCCESS", null);

        List<ChatMessageVO> msgVOs = conversationMapper.messagePoListToVoList(
                messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId()));

        return ChatResponseVO.builder()
                .conversationId(conversation.getId())
                .title(conversation.getTitle())
                .answer(answer)
                .messages(msgVOs)
                .createdAt(conversation.getCreatedAt())
                .build();
    }

    @Override
    public SseEmitter chatStream(ChatRequestDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        SseEmitter emitter = new SseEmitter(properties.getTimeout());

        sseExecutor.submit(() -> {
            long startTime = System.currentTimeMillis();
            StringBuilder fullAnswer = new StringBuilder();

            try {
                ConversationPO conversation = resolveConversation(dto, currentUser.getId());
                List<Map<String, String>> messages = buildMessageContext(conversation.getId(), dto.getMessage());
                saveMessage(conversation.getId(), "user", dto.getMessage(), null);

                sparkClient.chatStream(messages, String.valueOf(currentUser.getId()),
                        new XunFeiSparkClient.SparkStreamListener() {
                            @Override
                            public void onToken(String token) {
                                try {
                                    fullAnswer.append(token);
                                    emitter.send(SseEmitter.event().data(token));
                                } catch (Exception e) {
                                    log.warn("Failed to send SSE token", e);
                                }
                            }

                            @Override
                            public void onComplete(int promptTokens, int completionTokens) {
                                try {
                                    saveMessage(conversation.getId(), "assistant", fullAnswer.toString(), null);
                                    if (conversation.getTitle() == null || conversation.getTitle().isBlank()) {
                                        String msg = dto.getMessage();
                                        String title = msg.length() > 30 ? msg.substring(0, 30) + "..." : msg;
                                        conversation.setTitle(title);
                                        conversationRepository.save(conversation);
                                    }
                                    recordUsageStats(currentUser.getId(), "CHAT_STREAM", promptTokens,
                                            completionTokens, System.currentTimeMillis() - startTime, "SUCCESS", null);
                                    emitter.send(SseEmitter.event().name("done")
                                            .data("{\"conversationId\":" + conversation.getId() + "}"));
                                    emitter.complete();
                                } catch (Exception e) {
                                    log.error("SSE stream completion failed", e);
                                    emitter.completeWithError(e);
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                recordUsageStats(currentUser.getId(), "CHAT_STREAM", 0, 0,
                                        System.currentTimeMillis() - startTime, "FAILED", t.getMessage());
                                emitter.completeWithError(t);
                            }
                        });
            } catch (Exception e) {
                log.error("Failed to start SSE stream for user {}", currentUser.getId(), e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationVO> listConversations(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        return conversationMapper.poPageToVoPage(
                conversationRepository.findByUserIdOrderByUpdatedAtDesc(currentUser.getId(), pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageVO> getConversationMessages(Long conversationId) {
        User currentUser = SecurityUtils.getCurrentUser();
        ConversationPO conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(404, "对话不存在"));
        if (!conversation.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "无权访问该对话");
        }
        return conversationMapper.messagePoListToVoList(
                messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId));
    }

    @Override
    @Transactional
    public ConversationVO archiveConversation(Long conversationId) {
        User currentUser = SecurityUtils.getCurrentUser();
        ConversationPO conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(404, "对话不存在"));
        if (!conversation.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "无权操作该对话");
        }
        conversation.setStatus("ARCHIVED");
        return conversationMapper.poToVo(conversationRepository.save(conversation));
    }

    @Override
    @Transactional
    public void deleteConversation(Long conversationId) {
        User currentUser = SecurityUtils.getCurrentUser();
        ConversationPO conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(404, "对话不存在"));
        if (!conversation.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "无权删除该对话");
        }
        messageRepository.deleteAll(
                messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId));
        conversationRepository.delete(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiUsageStatsVO> getUsageStats(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        return usageStatsRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(po -> AiUsageStatsVO.builder()
                        .id(po.getId())
                        .userId(po.getUserId())
                        .model(po.getModel())
                        .requestType(po.getRequestType())
                        .promptTokens(po.getPromptTokens())
                        .completionTokens(po.getCompletionTokens())
                        .totalTokens(po.getTotalTokens())
                        .durationMs(po.getDurationMs())
                        .status(po.getStatus())
                        .createdAt(po.getCreatedAt())
                        .build());
    }

    // ── private helpers ────────────────────────────────────────────────────────

    private ConversationPO resolveConversation(ChatRequestDTO dto, Long userId) {
        if (dto.getConversationId() != null) {
            ConversationPO existing = conversationRepository.findById(dto.getConversationId())
                    .orElseThrow(() -> new BusinessException(404, "对话不存在"));
            if (!existing.getUserId().equals(userId)) {
                throw new BusinessException(403, "无权访问该对话");
            }
            return existing;
        }
        ConversationPO newConv = ConversationPO.builder()
                .userId(userId)
                .title(dto.getTitle())
                .model(properties.getModel())
                .status("ACTIVE")
                .build();
        return conversationRepository.save(newConv);
    }

    private List<Map<String, String>> buildMessageContext(Long conversationId, String newUserMessage) {
        List<ConversationMessagePO> history =
                messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        // Limit context window
        int start = Math.max(0, history.size() - MAX_CONTEXT_MESSAGES);
        List<Map<String, String>> messages = new ArrayList<>();
        for (int i = start; i < history.size(); i++) {
            ConversationMessagePO msg = history.get(i);
            Map<String, String> m = new HashMap<>();
            m.put("role", msg.getRole());
            m.put("content", msg.getContent());
            messages.add(m);
        }
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", newUserMessage);
        messages.add(userMsg);
        return messages;
    }

    @Transactional
    private void saveMessage(Long conversationId, String role, String content, Integer tokenCount) {
        ConversationMessagePO msg = ConversationMessagePO.builder()
                .conversationId(conversationId)
                .role(role)
                .content(content)
                .tokenCount(tokenCount)
                .build();
        messageRepository.save(msg);
    }

    private void recordUsageStats(Long userId, String requestType,
                                  int promptTokens, int completionTokens,
                                  long durationMs, String status, String errorMessage) {
        if (!properties.isEnableMonitoring()) {
            return;
        }
        try {
            AiUsageStatsPO stats = AiUsageStatsPO.builder()
                    .userId(userId)
                    .model(properties.getModel())
                    .requestType(requestType)
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(promptTokens + completionTokens)
                    .durationMs(durationMs)
                    .status(status)
                    .errorMessage(errorMessage)
                    .build();
            usageStatsRepository.save(stats);
        } catch (Exception e) {
            log.warn("Failed to record AI usage stats", e);
        }
    }
}
