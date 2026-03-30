package com.coursebuddy.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.auth.User;
import com.coursebuddy.client.AIChatClient;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.config.AIProperties;
import com.coursebuddy.converter.ConversationConverter;
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
import com.coursebuddy.mapper.AiUsageStatsMapper;
import com.coursebuddy.mapper.ConversationMapper;
import com.coursebuddy.mapper.ConversationMessageMapper;
import com.coursebuddy.mapper.KnowledgeItemMapper;
import com.coursebuddy.service.IAIChatService;
import com.coursebuddy.util.RateLimiter;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatServiceImpl implements IAIChatService {

    private final AIChatClient aiChatClient;
    private final AIProperties properties;
    private final RateLimiter rateLimiter;
    private final ConversationMapper conversationRepository;
    private final ConversationMessageMapper messageRepository;
    private final AiUsageStatsMapper usageStatsRepository;
    private final KnowledgeItemMapper knowledgeItemRepository;
    private final ConversationConverter conversationMapper;

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
        ensureConfigured();
        User currentUser = SecurityUtils.getCurrentUser();
        ensureRateLimit(currentUser.getId());
        long startTime = System.currentTimeMillis();

        ConversationPO conversation = resolveConversation(dto, currentUser.getId());
        List<KnowledgeItemPO> sourceItems = findRelevantKnowledgeItems(dto);
        List<KnowledgeSourceVO> sources = toSourceVOs(sourceItems);
        List<ChatMessage> messages = buildMessageContext(conversation.getId(), dto, sourceItems);

        AIChatClient.AIChatResult result;
        try {
            result = aiChatClient.chat(messages);
        } catch (Exception e) {
            logModelError("同步对话失败，userId=" + currentUser.getId(), e);
            recordUsageStats(currentUser.getId(), "CHAT", 0, 0,
                    System.currentTimeMillis() - startTime, "FAILED", e.getMessage());
            throw new BusinessException(500, "AI 服务暂时不可用，请稍后重试");
        }

        saveMessage(conversation.getId(), "user", dto.getMessage(), null);
        saveMessage(conversation.getId(), "assistant", result.content(), null);
        updateConversationTitle(conversation, dto.getMessage());

        recordUsageStats(currentUser.getId(), "CHAT", result.promptTokens(), result.completionTokens(),
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
        ensureConfigured();
        User currentUser = SecurityUtils.getCurrentUser();
        ensureRateLimit(currentUser.getId());
        SseEmitter emitter = new SseEmitter(properties.getTimeout());
        AtomicBoolean emitterCompleted = new AtomicBoolean(false);

        sseExecutor.submit(() -> {
            long startTime = System.currentTimeMillis();
            StringBuilder fullAnswer = new StringBuilder();

            try {
                ConversationPO conversation = resolveConversation(dto, currentUser.getId());
                List<KnowledgeItemPO> sourceItems = findRelevantKnowledgeItems(dto);
                List<KnowledgeSourceVO> sources = toSourceVOs(sourceItems);
                List<ChatMessage> messages = buildMessageContext(conversation.getId(), dto, sourceItems);
                saveMessage(conversation.getId(), "user", dto.getMessage(), null);

                aiChatClient.chatStream(messages, new AIChatClient.AIStreamListener() {
                    @Override
                    public void onToken(String token) {
                        if (emitterCompleted.get()) {
                            return;
                        }
                        try {
                            fullAnswer.append(token);
                            emitter.send(SseEmitter.event().data(token));
                        } catch (IOException e) {
                            emitterCompleted.set(true);
                            safeCompleteWithError(emitter, e);
                        } catch (Exception e) {
                            emitterCompleted.set(true);
                        }
                    }

                    @Override
                    public void onComplete(int promptTokens, int completionTokens) {
                        if (!emitterCompleted.compareAndSet(false, true)) {
                            return;
                        }
                        try {
                            saveMessage(conversation.getId(), "assistant", fullAnswer.toString(), null);
                            updateConversationTitle(conversation, dto.getMessage());
                            recordUsageStats(currentUser.getId(), "CHAT_STREAM", promptTokens, completionTokens,
                                    System.currentTimeMillis() - startTime, "SUCCESS", null);
                            emitter.send(SseEmitter.event()
                                    .name("done")
                                    .data(buildDoneEventPayload(conversation.getId(), sources)));
                        } catch (Exception e) {
                            logModelError("流式对话完成事件处理失败，userId=" + currentUser.getId(), e);
                        } finally {
                            safeComplete(emitter);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (!emitterCompleted.compareAndSet(false, true)) {
                            return;
                        }
                        recordUsageStats(currentUser.getId(), "CHAT_STREAM", 0, 0,
                                System.currentTimeMillis() - startTime, "FAILED", throwable.getMessage());
                        safeCompleteWithError(emitter, throwable);
                    }
                });
            } catch (Exception e) {
                logModelError("流式对话启动失败，userId=" + currentUser.getId(), e);
                if (!emitterCompleted.compareAndSet(false, true)) {
                    return;
                }
                safeCompleteWithError(emitter, e);
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

    private void ensureConfigured() {
        if (!properties.isEnabled()) {
            throw new BusinessException(503, "AI 服务未启用");
        }
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new BusinessException(500, "AI 服务尚未配置 API Key");
        }
    }

    private void ensureRateLimit(Long userId) {
        if (!rateLimiter.tryAcquire(userId, properties.getRateLimitPerMinute())) {
            throw new BusinessException(429, "AI 请求过于频繁，请稍后重试");
        }
    }

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

    private List<ChatMessage> buildMessageContext(Long conversationId, ChatRequestDTO dto, List<KnowledgeItemPO> sourceItems) {
        List<ChatMessage> messages = new ArrayList<>();

        if (properties.getSystemPrompt() != null && !properties.getSystemPrompt().isBlank()) {
            messages.add(SystemMessage.from(properties.getSystemPrompt()));
        }

        if (dto.isIncludeHistory()) {
            List<ConversationMessagePO> history = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
            int start = Math.max(0, history.size() - properties.getMaxContextMessages());
            for (int i = start; i < history.size(); i++) {
                ChatMessage chatMessage = toChatMessage(history.get(i));
                if (chatMessage != null) {
                    messages.add(chatMessage);
                }
            }
        }

        messages.add(UserMessage.from(mergeMessageWithKnowledge(dto.getMessage(), sourceItems)));
        return messages;
    }

    private ChatMessage toChatMessage(ConversationMessagePO message) {
        if (message.getContent() == null || message.getContent().isBlank()) {
            return null;
        }
        return switch (defaultIfBlank(message.getRole(), "user").toLowerCase(Locale.ROOT)) {
            case "assistant" -> AiMessage.from(message.getContent());
            case "system" -> SystemMessage.from(message.getContent());
            default -> UserMessage.from(message.getContent());
        };
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
                .sorted(Comparator.comparingInt((KnowledgeItemPO item) -> relevanceScore(item, lowerKeyword)).reversed())
                .limit(properties.getMaxSourceItems())
                .toList();
    }

    private int relevanceScore(KnowledgeItemPO item, String lowerKeyword) {
        int score = 0;
        if (item.getTitle() != null && item.getTitle().toLowerCase(Locale.ROOT).contains(lowerKeyword)) {
            score += 4;
        }
        if (item.getTags() != null && item.getTags().toLowerCase(Locale.ROOT).contains(lowerKeyword)) {
            score += 3;
        }
        if (item.getDescription() != null && item.getDescription().toLowerCase(Locale.ROOT).contains(lowerKeyword)) {
            score += 2;
        }
        if (item.getContent() != null && item.getContent().toLowerCase(Locale.ROOT).contains(lowerKeyword)) {
            score += 1;
        }
        return score;
    }

    private List<KnowledgeSourceVO> toSourceVOs(List<KnowledgeItemPO> sourceItems) {
        return sourceItems.stream()
                .map(item -> KnowledgeSourceVO.builder()
                        .knowledgeItemId(item.getId())
                        .title(item.getTitle())
                        .snippet(shortenSnippet(item.getContent() != null ? item.getContent() : item.getDescription(), 180))
                        .sourceType(item.getSourceType())
                        .build())
                .toList();
    }

    private String mergeMessageWithKnowledge(String originalMessage, List<KnowledgeItemPO> sourceItems) {
        if (sourceItems.isEmpty()) {
            return originalMessage;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("请优先基于课程知识库回答，回答末尾给出你使用的知识条目编号，例如 [K12]。\n");
        builder.append("知识库上下文：\n");
        for (KnowledgeItemPO item : sourceItems) {
            builder.append("[K").append(item.getId()).append("] ")
                    .append(defaultIfBlank(item.getTitle(), "Untitled"))
                    .append("\n")
                    .append(shortenSnippet(item.getContent() != null ? item.getContent() : item.getDescription(), 280))
                    .append("\n");
        }
        builder.append("用户问题：").append(originalMessage);
        return builder.toString();
    }

    private String shortenSnippet(String text, int maxLen) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLen) {
            return normalized;
        }
        return normalized.substring(0, maxLen).trim() + "...";
    }

    private String buildDoneEventPayload(Long conversationId, List<KnowledgeSourceVO> sources) {
        String sourceJson = sources.stream()
                .map(source -> "{\"knowledgeItemId\":" + source.getKnowledgeItemId()
                        + ",\"title\":\"" + escapeJson(defaultIfBlank(source.getTitle(), "")) + "\""
                        + ",\"snippet\":\"" + escapeJson(defaultIfBlank(source.getSnippet(), "")) + "\"}")
                .collect(Collectors.joining(","));
        return "{\"conversationId\":" + conversationId + ",\"sources\":[" + sourceJson + "]}";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void saveMessage(Long conversationId, String role, String content, Integer tokenCount) {
        ConversationMessagePO message = ConversationMessagePO.builder()
                .conversationId(conversationId)
                .role(role)
                .content(content)
                .tokenCount(tokenCount)
                .build();
        messageRepository.insert(message);
    }

    private void updateConversationTitle(ConversationPO conversation, String question) {
        if (conversation.getTitle() != null && !conversation.getTitle().isBlank()) {
            return;
        }
        String title = question.length() > 30 ? question.substring(0, 30) + "..." : question;
        conversation.setTitle(title);
        conversationRepository.updateById(conversation);
    }

    private void safeComplete(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception ignored) {
        }
    }

    private void safeCompleteWithError(SseEmitter emitter, Throwable throwable) {
        try {
            emitter.completeWithError(throwable);
        } catch (Exception ignored) {
        }
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void logModelError(String message, Throwable throwable) {
        if (properties.isEnableErrorLogging()) {
            log.error(message, throwable);
        }
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
