package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.client.XunFeiSparkClient;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.config.XunFeiProperties;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.ChatRequestDTO;
import com.coursebuddy.domain.po.AiUsageStatsPO;
import com.coursebuddy.domain.po.ConversationMessagePO;
import com.coursebuddy.domain.po.ConversationPO;
import com.coursebuddy.domain.po.KnowledgeItemPO;
import com.coursebuddy.domain.vo.AiUsageStatsVO;
import com.coursebuddy.domain.vo.ChatMessageVO;
import com.coursebuddy.domain.vo.ChatResponseVO;
import com.coursebuddy.domain.vo.ConversationVO;
import com.coursebuddy.domain.vo.KnowledgeSourceVO;
import com.coursebuddy.converter.ConversationConverter;
import com.coursebuddy.mapper.AiUsageStatsMapper;
import com.coursebuddy.mapper.ConversationMessageMapper;
import com.coursebuddy.mapper.ConversationMapper;
import com.coursebuddy.mapper.KnowledgeItemMapper;
import com.coursebuddy.service.IXunFeiAiService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class XunFeiAiServiceImpl implements IXunFeiAiService {

    private final XunFeiSparkClient sparkClient;
    private final XunFeiProperties properties;
    private final ConversationMapper conversationRepository;
    private final ConversationMessageMapper messageRepository;
    private final AiUsageStatsMapper usageStatsRepository;
    private final KnowledgeItemMapper knowledgeItemRepository;
    private final ConversationConverter conversationMapper;

    private static final int MAX_CONTEXT_MESSAGES = 20;
    private static final int MAX_SOURCE_ITEMS = 3;
    private final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    @PreDestroy
    public void shutdownExecutor() {
        sseExecutor.shutdown();
        try {
            if (!sseExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                sseExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            sseExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    @Transactional
    public ChatResponseVO chat(ChatRequestDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        long startTime = System.currentTimeMillis();
        String requestType = "CHAT";

        ConversationPO conversation = resolveConversation(dto, currentUser.getId());

        List<KnowledgeItemPO> sourceItems = findRelevantKnowledgeItems(dto);
        List<KnowledgeSourceVO> sources = toSourceVOs(sourceItems);
        String userMessageForModel = mergeMessageWithKnowledge(dto.getMessage(), sourceItems);
        List<Map<String, String>> messages = buildMessageContext(conversation.getId(), userMessageForModel);

        XunFeiSparkClient.SparkChatResult result;
        try {
            result = sparkClient.chat(messages, String.valueOf(currentUser.getId()));
        } catch (Exception e) {
            log.error("XunFei chat failed for user {}", currentUser.getId(), e);
            recordUsageStats(currentUser.getId(), requestType, 0, 0,
                    System.currentTimeMillis() - startTime, "FAILED", e.getMessage());
            throw new BusinessException(500, "AI 服务暂时不可用，请稍后重试");
        }

        // Save user message
        saveMessage(conversation.getId(), "user", dto.getMessage(), null);
        // Save assistant message
        saveMessage(conversation.getId(), "assistant", result.content(), null);

        // Update conversation title if new
        if (conversation.getTitle() == null || conversation.getTitle().isBlank()) {
            String title = dto.getMessage().length() > 30
                    ? dto.getMessage().substring(0, 30) + "..."
                    : dto.getMessage();
            conversation.setTitle(title);
            conversationRepository.updateById(conversation);
        }

        recordUsageStats(currentUser.getId(), requestType, result.promptTokens(), result.completionTokens(),
                System.currentTimeMillis() - startTime, "SUCCESS", null);

        List<ChatMessageVO> msgVOs = conversationMapper.messagePoListToVoList(
                messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId()));

        return ChatResponseVO.builder()
                .conversationId(conversation.getId())
                .title(conversation.getTitle())
                .answer(result.content())
                .messages(msgVOs)
                .sources(sources)
                .relatedKnowledgeIds(sources.stream()
                        .map(KnowledgeSourceVO::getKnowledgeItemId)
                        .collect(Collectors.toList()))
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
                List<KnowledgeItemPO> sourceItems = findRelevantKnowledgeItems(dto);
                List<KnowledgeSourceVO> sources = toSourceVOs(sourceItems);
                String userMessageForModel = mergeMessageWithKnowledge(dto.getMessage(), sourceItems);
                List<Map<String, String>> messages = buildMessageContext(conversation.getId(), userMessageForModel);
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
                                        conversationRepository.updateById(conversation);
                                    }
                                    recordUsageStats(currentUser.getId(), "CHAT_STREAM", promptTokens,
                                            completionTokens, System.currentTimeMillis() - startTime, "SUCCESS", null);
                                    emitter.send(SseEmitter.event().name("done")
                                            .data(buildDoneEventPayload(conversation.getId(), sources)));
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
        IPage<ConversationPO> poPage = conversationRepository.findByUserIdOrderByUpdatedAtDesc(
                MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId());
        return conversationMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageVO> getConversationMessages(Long conversationId) {
        User currentUser = SecurityUtils.getCurrentUser();
        ConversationPO conversation = conversationRepository.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(404, "对话不存在");
        }
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
        ConversationPO conversation = conversationRepository.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(404, "对话不存在");
        }
        if (!conversation.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "无权操作该对话");
        }
        conversation.setStatus("ARCHIVED");
        conversationRepository.updateById(conversation);
        return conversationMapper.poToVo(conversation);
    }

    @Override
    @Transactional
    public void deleteConversation(Long conversationId) {
        User currentUser = SecurityUtils.getCurrentUser();
        ConversationPO conversation = conversationRepository.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(404, "对话不存在");
        }
        if (!conversation.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "无权删除该对话");
        }
        messageRepository.deleteByConversationId(conversationId);
        conversationRepository.deleteById(conversation.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiUsageStatsVO> getUsageStats(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        IPage<AiUsageStatsPO> poPage = usageStatsRepository.findByUserIdOrderByCreatedAtDesc(
                MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId());
        return MybatisPlusPageUtils.toSpringPage(poPage, pageable)
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
            ConversationPO existing = conversationRepository.selectById(dto.getConversationId());
            if (existing == null) {
                throw new BusinessException(404, "对话不存在");
            }
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
        conversationRepository.insert(newConv);
        return newConv;
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

    private List<KnowledgeItemPO> findRelevantKnowledgeItems(ChatRequestDTO dto) {
        if (!dto.isIncludeKnowledgeContext() || dto.getCourseId() == null) {
            return List.of();
        }
        String keyword = dto.getMessage() == null ? "" : dto.getMessage().trim();
        if (keyword.isBlank()) {
            return List.of();
        }

        List<KnowledgeItemPO> candidates = knowledgeItemRepository.searchByCourseAndKeyword(dto.getCourseId(), keyword);
        if (candidates.isEmpty()) {
            return List.of();
        }

        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        return candidates.stream()
                .sorted(Comparator.comparingInt((KnowledgeItemPO i) -> relevanceScore(i, lowerKeyword)).reversed())
                .limit(MAX_SOURCE_ITEMS)
                .toList();
    }

    private int relevanceScore(KnowledgeItemPO item, String lowerKeyword) {
        int score = 0;
        if (item.getTitle() != null && item.getTitle().toLowerCase(Locale.ROOT).contains(lowerKeyword)) score += 4;
        if (item.getTags() != null && item.getTags().toLowerCase(Locale.ROOT).contains(lowerKeyword)) score += 3;
        if (item.getDescription() != null && item.getDescription().toLowerCase(Locale.ROOT).contains(lowerKeyword)) score += 2;
        if (item.getContent() != null && item.getContent().toLowerCase(Locale.ROOT).contains(lowerKeyword)) score += 1;
        return score;
    }

    private List<KnowledgeSourceVO> toSourceVOs(List<KnowledgeItemPO> sourceItems) {
        return sourceItems.stream().map(item -> KnowledgeSourceVO.builder()
                .knowledgeItemId(item.getId())
                .title(item.getTitle())
                .snippet(shortenSnippet(item.getContent() != null ? item.getContent() : item.getDescription(), 180))
                .sourceType(item.getSourceType())
                .build()).toList();
    }

    private String mergeMessageWithKnowledge(String originalMessage, List<KnowledgeItemPO> sourceItems) {
        if (sourceItems.isEmpty()) {
            return originalMessage;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("请优先基于课程知识库回答，回答末尾给出你使用的知识条目编号，例如 [K12]。\n");
        sb.append("知识库上下文：\n");
        for (KnowledgeItemPO item : sourceItems) {
            sb.append("[K").append(item.getId()).append("] ")
                    .append(defaultIfBlank(item.getTitle(), "Untitled")).append("\n")
                    .append(shortenSnippet(item.getContent() != null ? item.getContent() : item.getDescription(), 280))
                    .append("\n");
        }
        sb.append("用户问题：").append(originalMessage);
        return sb.toString();
    }

    private String shortenSnippet(String text, int maxLen) {
        if (text == null || text.isBlank()) return "";
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLen) return normalized;
        return normalized.substring(0, maxLen).trim() + "...";
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String buildDoneEventPayload(Long conversationId, List<KnowledgeSourceVO> sources) {
        String sourceJson = sources.stream()
                .map(s -> "{\"knowledgeItemId\":" + s.getKnowledgeItemId()
                        + ",\"title\":\"" + escapeJson(defaultIfBlank(s.getTitle(), "")) + "\""
                        + ",\"snippet\":\"" + escapeJson(defaultIfBlank(s.getSnippet(), "")) + "\"}")
                .collect(Collectors.joining(","));
        return "{\"conversationId\":" + conversationId + ",\"sources\":[" + sourceJson + "]}";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void saveMessage(Long conversationId, String role, String content, Integer tokenCount) {
        ConversationMessagePO msg = ConversationMessagePO.builder()
                .conversationId(conversationId)
                .role(role)
                .content(content)
                .tokenCount(tokenCount)
                .build();
        messageRepository.insert(msg);
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
            usageStatsRepository.insert(stats);
        } catch (Exception e) {
            log.warn("Failed to record AI usage stats", e);
        }
    }
}
