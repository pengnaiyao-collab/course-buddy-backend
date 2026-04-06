package com.coursebuddy.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.client.AIChatClient;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.config.AIProperties;
import com.coursebuddy.converter.ConversationConverter;
import com.coursebuddy.domain.auth.User;
import com.coursebuddy.domain.dto.ChatImageDTO;
import com.coursebuddy.domain.dto.ChatRequestDTO;
import com.coursebuddy.domain.po.ConversationMessagePO;
import com.coursebuddy.domain.po.ConversationPO;
import com.coursebuddy.domain.po.NotePO;
import com.coursebuddy.domain.vo.ChatMessageVO;
import com.coursebuddy.domain.vo.ChatResponseVO;
import com.coursebuddy.domain.vo.ConversationVO;
import com.coursebuddy.domain.vo.KnowledgeSourceVO;
import com.coursebuddy.mapper.ConversationMapper;
import com.coursebuddy.mapper.ConversationMessageMapper;
import com.coursebuddy.mapper.NoteMapper;
import com.coursebuddy.service.IAIChatService;
import com.coursebuddy.util.RateLimiter;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * AI聊天服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatServiceImpl implements IAIChatService {

    private final AIChatClient aiChatClient;
    private final AIProperties properties;
    private final ConversationMapper conversationRepository;
    private final ConversationMessageMapper messageRepository;
    private final ConversationConverter conversationMapper;
    private final NoteMapper noteRepository;
    private final RateLimiter rateLimiter;

    @Override
    @Transactional
    public ChatResponseVO chat(ChatRequestDTO dto) {
        ensureConfigured();
        User currentUser = SecurityUtils.getCurrentUser();
        ensureRateLimit(currentUser.getId());

        ConversationPO conversation = resolveConversation(dto, currentUser.getId());
        List<NotePO> sourceItems = findRelevantNotes(dto, currentUser.getId());
        List<ChatMessage> messages = buildMessageContext(conversation.getId(), dto, sourceItems);

        AIChatClient.AIChatResult result;
        try {
            result = aiChatClient.chat(messages);
        } catch (Exception ex) {
            logModelError("同步对话失败，userId=" + currentUser.getId(), ex);
            throw new BusinessException(500, "AI 服务调用失败，请稍后重试");
        }

        String answer = defaultIfBlank(result.content(), "");
        ChatImageDTO image = firstImage(dto);
        saveMessage(conversation.getId(), "user", dto.getMessage(), result.promptTokens(), image);
        saveMessage(conversation.getId(), "assistant", answer, result.completionTokens());
        updateConversationTitle(conversation, dto.getMessage());

        List<KnowledgeSourceVO> sources = toSourceVOs(sourceItems);
        List<Long> sourceIds = sources.stream()
                .map(KnowledgeSourceVO::getKnowledgeItemId)
                .filter(Objects::nonNull)
                .toList();

        return ChatResponseVO.builder()
                .conversationId(conversation.getId())
                .title(conversation.getTitle())
                .answer(answer)
                .sources(sources)
                .relatedKnowledgeIds(sourceIds)
                .build();
    }

    @Override
    public SseEmitter chatStream(ChatRequestDTO dto) {
        ensureConfigured();
        User currentUser = SecurityUtils.getCurrentUser();
        ensureRateLimit(currentUser.getId());

        ConversationPO conversation = resolveConversation(dto, currentUser.getId());
        List<NotePO> sourceItems = findRelevantNotes(dto, currentUser.getId());
        List<ChatMessage> messages = buildMessageContext(conversation.getId(), dto, sourceItems);

        SseEmitter emitter = new SseEmitter(0L);
        AtomicBoolean emitterCompleted = new AtomicBoolean(false);
        StringBuilder assistantBuffer = new StringBuilder();
        ChatImageDTO image = firstImage(dto);

        emitter.onCompletion(() -> emitterCompleted.set(true));
        emitter.onTimeout(() -> emitterCompleted.set(true));

        CompletableFuture.runAsync(() -> {
            try {
                aiChatClient.chatStream(messages, new AIChatClient.AIStreamListener() {
                    @Override
                    public void onToken(String token) {
                        if (emitterCompleted.get()) {
                            return;
                        }
                        assistantBuffer.append(token);
                        try {
                            emitter.send(SseEmitter.event().data(token));
                        } catch (Exception ex) {
                            emitterCompleted.set(true);
                            safeCompleteWithError(emitter, ex);
                        }
                    }

                    @Override
                    public void onComplete(int promptTokensValue, int completionTokensValue) {
                        if (emitterCompleted.get()) {
                            return;
                        }
                        String answer = assistantBuffer.toString();
                        saveMessage(conversation.getId(), "user", dto.getMessage(), promptTokensValue, image);
                        saveMessage(conversation.getId(), "assistant", answer, completionTokensValue);
                        updateConversationTitle(conversation, dto.getMessage());

                        try {
                            List<KnowledgeSourceVO> sources = toSourceVOs(sourceItems);
                            String donePayload = buildDoneEventPayload(conversation.getId(), sources);
                            emitter.send(SseEmitter.event().name("done").data(donePayload));
                        } catch (Exception ex) {
                            logModelError("发送完成事件失败，userId=" + currentUser.getId(), ex);
                        } finally {
                            emitterCompleted.set(true);
                            safeComplete(emitter);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (emitterCompleted.get()) {
                            return;
                        }
                        logModelError("流式对话失败，userId=" + currentUser.getId(), throwable);
                        emitterCompleted.set(true);
                        safeCompleteWithError(emitter, throwable);
                    }
                });
            } catch (Exception ex) {
                logModelError("流式对话启动失败，userId=" + currentUser.getId(), ex);
                if (!emitterCompleted.compareAndSet(false, true)) {
                    return;
                }
                safeCompleteWithError(emitter, ex);
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

    private List<ChatMessage> buildMessageContext(Long conversationId, ChatRequestDTO dto, List<NotePO> sourceItems) {
        List<ChatMessage> messages = new ArrayList<>();

        if (properties.getSystemPrompt() != null && !properties.getSystemPrompt().isBlank()) {
            messages.add(SystemMessage.from(properties.getSystemPrompt()));
        }

        if (dto.isIncludeHistory()) {
            List<ConversationMessagePO> history = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
            if (history != null && !history.isEmpty()) {
                int start = Math.max(0, history.size() - properties.getMaxContextMessages());
                for (int i = start; i < history.size(); i++) {
                    ChatMessage chatMessage = toChatMessage(history.get(i));
                    if (chatMessage != null) {
                        messages.add(chatMessage);
                    }
                }
            }
        }

        messages.add(buildUserMessage(dto, sourceItems));
        return messages;
    }

    private UserMessage buildUserMessage(ChatRequestDTO dto, List<NotePO> sourceItems) {
        String prompt = mergeMessageWithKnowledge(dto.getMessage(), sourceItems);
        List<Content> contents = new ArrayList<>();
        contents.add(TextContent.from(prompt));

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (ChatImageDTO image : dto.getImages()) {
                ImageContent imageContent = toImageContent(image);
                if (imageContent != null) {
                    contents.add(imageContent);
                }
            }
        }

        return UserMessage.from(contents);
    }

    private ImageContent toImageContent(ChatImageDTO image) {
        if (image == null) {
            return null;
        }
        String base64 = normalizeBase64(image.getBase64Data());
        if (base64 == null || base64.isBlank()) {
            return null;
        }
        String mimeType = resolveMimeType(image.getMimeType(), image.getBase64Data());
        Image img = Image.builder()
                .base64Data(base64)
                .mimeType(mimeType)
                .build();
        return ImageContent.from(img);
    }

    private String normalizeBase64(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        int base64Index = trimmed.indexOf("base64,");
        if (base64Index >= 0) {
            return trimmed.substring(base64Index + 7).replaceAll("\\s", "");
        }
        if (trimmed.startsWith("data:") && trimmed.contains(",")) {
            return trimmed.substring(trimmed.indexOf(',') + 1).replaceAll("\\s", "");
        }
        return trimmed.replaceAll("\\s", "");
    }

    private String resolveMimeType(String explicitMimeType, String rawBase64) {
        if (explicitMimeType != null && !explicitMimeType.isBlank()) {
            return explicitMimeType;
        }
        if (rawBase64 != null && rawBase64.startsWith("data:") && rawBase64.contains(";base64,")) {
            String prefix = rawBase64.substring("data:".length(), rawBase64.indexOf(";base64,"));
            if (!prefix.isBlank()) {
                return prefix;
            }
        }
        return "image/png";
    }

    private ChatMessage toChatMessage(ConversationMessagePO message) {
        if ((message.getContent() == null || message.getContent().isBlank())
                && (message.getImageData() == null || message.getImageData().isBlank())) {
            return null;
        }
        return switch (defaultIfBlank(message.getRole(), "user").toLowerCase(Locale.ROOT)) {
            case "assistant" -> AiMessage.from(message.getContent());
            case "system" -> SystemMessage.from(message.getContent());
            default -> buildUserMessageFromHistory(message);
        };
    }

    private UserMessage buildUserMessageFromHistory(ConversationMessagePO message) {
        String content = message.getContent();
        String text = content == null || content.isBlank() ? "请识别这张图片" : content;
        List<Content> contents = new ArrayList<>();
        contents.add(TextContent.from(text));

        ImageContent imageContent = toImageContent(message.getImageData(), message.getImageMimeType());
        if (imageContent != null) {
            contents.add(imageContent);
        }

        return UserMessage.from(contents);
    }

    private ImageContent toImageContent(String base64Data, String mimeType) {
        String base64 = normalizeBase64(base64Data);
        if (base64 == null || base64.isBlank()) {
            return null;
        }
        Image img = Image.builder()
                .base64Data(base64)
                .mimeType(resolveMimeType(mimeType, base64Data))
                .build();
        return ImageContent.from(img);
    }

    private List<NotePO> findRelevantNotes(ChatRequestDTO dto, Long userId) {
        if (!dto.isIncludeKnowledgeContext()) {
            return List.of();
        }
        String keyword = dto.getMessage() == null ? "" : dto.getMessage().trim();
        if (keyword.isBlank()) {
            return List.of();
        }

        int fetchSize = Math.max(properties.getMaxSourceItems() * 5, 10);
        IPage<NotePO> poPage = noteRepository.findByUserIdAndTitleContainingIgnoreCaseAndIsDeletedFalse(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, fetchSize), userId, keyword);
        List<NotePO> candidates = poPage.getRecords();
        if (candidates.isEmpty()) {
            return List.of();
        }

        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        return candidates.stream()
                .sorted(Comparator.comparingInt((NotePO note) -> relevanceScore(note, lowerKeyword)).reversed())
                .limit(properties.getMaxSourceItems())
                .toList();
    }

    private int relevanceScore(NotePO note, String lowerKeyword) {
        int score = 0;
        if (note.getTitle() != null && note.getTitle().toLowerCase(Locale.ROOT).contains(lowerKeyword)) {
            score += 4;
        }
        if (note.getContent() != null && note.getContent().toLowerCase(Locale.ROOT).contains(lowerKeyword)) {
            score += 1;
        }
        return score;
    }

    private List<KnowledgeSourceVO> toSourceVOs(List<NotePO> sourceItems) {
        if (sourceItems == null || sourceItems.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceItems.stream()
                .map(item -> KnowledgeSourceVO.builder()
                        .knowledgeItemId(item.getId())
                        .title(item.getTitle())
                        .snippet(shortenSnippet(item.getContent(), 180))
                        .sourceType("NOTE")
                        .build())
                .toList();
    }

    private String mergeMessageWithKnowledge(String originalMessage, List<NotePO> sourceItems) {
        if (sourceItems.isEmpty()) {
            return originalMessage;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("请优先基于个人知识库（笔记）回答，回答末尾给出你使用的笔记编号，例如 [N12]。\n");
        builder.append("知识库上下文：\n");
        for (NotePO item : sourceItems) {
            builder.append("[N").append(item.getId()).append("] ")
                    .append(defaultIfBlank(item.getTitle(), "Untitled"))
                    .append("\n")
                    .append(shortenSnippet(item.getContent(), 280))
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
        saveMessage(conversationId, role, content, tokenCount, null);
    }

    private void saveMessage(Long conversationId, String role, String content, Integer tokenCount, ChatImageDTO image) {
        ConversationMessagePO message = ConversationMessagePO.builder()
                .conversationId(conversationId)
                .role(role)
                .content(content)
                .tokenCount(tokenCount)
                .build();

        if (image != null) {
            message.setImageData(normalizeBase64(image.getBase64Data()));
            message.setImageMimeType(resolveMimeType(image.getMimeType(), image.getBase64Data()));
            message.setImageName(image.getFileName());
        }

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

    private ChatImageDTO firstImage(ChatRequestDTO dto) {
        if (dto.getImages() == null || dto.getImages().isEmpty()) {
            return null;
        }
        return dto.getImages().get(0);
    }
}
