package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.client.AIChatClient;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.config.AIProperties;
import com.coursebuddy.domain.dto.GenerateContentDTO;
import com.coursebuddy.domain.po.AiUsageStatsPO;
import com.coursebuddy.domain.po.GeneratedContentPO;
import com.coursebuddy.domain.vo.GeneratedContentVO;
import com.coursebuddy.converter.GeneratedContentConverter;
import com.coursebuddy.mapper.AiUsageStatsMapper;
import com.coursebuddy.mapper.GeneratedContentMapper;
import com.coursebuddy.mapper.KnowledgeItemMapper;
import com.coursebuddy.service.IContentGeneratorService;
import com.coursebuddy.util.AIResponseCache;
import com.coursebuddy.util.RateLimiter;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentGeneratorServiceImpl implements IContentGeneratorService {

    private final AIChatClient aiChatClient;
    private final AIProperties properties;
    private final AIResponseCache aiResponseCache;
    private final RateLimiter rateLimiter;
    private final GeneratedContentMapper contentRepository;
    private final AiUsageStatsMapper usageStatsRepository;
    private final KnowledgeItemMapper knowledgeItemRepository;
    private final GeneratedContentConverter contentMapper;

    @Override
    @Transactional
    public GeneratedContentVO generateOutline(GenerateContentDTO dto) {
        dto.setContentType("OUTLINE");
        return generate(dto);
    }

    @Override
    @Transactional
    public GeneratedContentVO generateExamPoints(GenerateContentDTO dto) {
        dto.setContentType("EXAM_POINTS");
        return generate(dto);
    }

    @Override
    @Transactional
    public GeneratedContentVO generateQuestions(GenerateContentDTO dto) {
        dto.setContentType("QUESTIONS");
        return generate(dto);
    }

    @Override
    @Transactional
    public GeneratedContentVO generateBreakdown(GenerateContentDTO dto) {
        dto.setContentType("BREAKDOWN");
        return generate(dto);
    }

    @Override
    @Transactional
    public GeneratedContentVO generateLearningPath(GenerateContentDTO dto) {
        dto.setContentType("LEARNING_PATH");
        return generate(dto);
    }

    @Override
    @Transactional
    public GeneratedContentVO generateQuestionExplanation(GenerateContentDTO dto) {
        dto.setContentType("EXPLANATION");
        return generate(dto);
    }

    @Override
    @Transactional
    public GeneratedContentVO generateMnemonic(GenerateContentDTO dto) {
        dto.setContentType("MNEMONIC");
        return generate(dto);
    }

    @Override
    @Transactional
    public GeneratedContentVO generateReportFramework(GenerateContentDTO dto) {
        dto.setContentType("REPORT_FRAMEWORK");
        return generate(dto);
    }

    @Override
    @Transactional
    public GeneratedContentVO generateCorePoints(GenerateContentDTO dto) {
        dto.setContentType("CORE_POINTS");
        return generate(dto);
    }

    @Override
    @Transactional
    public GeneratedContentVO generateDataProcessingPlan(GenerateContentDTO dto) {
        dto.setContentType("DATA_PROCESSING");
        return generate(dto);
    }

    @Override
    @Transactional
    public GeneratedContentVO generate(GenerateContentDTO dto) {
        ensureConfigured();
        User currentUser = SecurityUtils.getCurrentUser();
        ensureRateLimit(currentUser.getId());
        long startTime = System.currentTimeMillis();

        String prompt = buildPrompt(dto);
        String cacheKey = buildCacheKey(dto);
        String cachedContent = loadCachedContent(cacheKey);

        GeneratedContentPO po = GeneratedContentPO.builder()
                .userId(currentUser.getId())
                .contentType(dto.getContentType())
                .subject(dto.getSubject())
                .courseId(dto.getCourseId())
                .prompt(prompt)
                .status("PENDING")
                .build();
        contentRepository.insert(po);

        try {
            String finalContent;
            int promptTokens = 0;
            int completionTokens = 0;

            if (cachedContent != null) {
                finalContent = cachedContent;
            } else {
                List<ChatMessage> messages = buildMessages(prompt);
                AIChatClient.AIChatResult result = aiChatClient.chat(messages);
                finalContent = result.content();
                promptTokens = result.promptTokens();
                completionTokens = result.completionTokens();
                cacheContent(cacheKey, finalContent);
            }

            if ("LEARNING_PATH".equalsIgnoreCase(dto.getContentType())) {
                finalContent = appendRecommendedResources(finalContent, dto.getCourseId(), dto.getSubject());
            }

            po.setContent(finalContent);
            po.setStatus("COMPLETED");
            po.setTokenCount(promptTokens + completionTokens);
            contentRepository.updateById(po);

            recordUsageStats(currentUser.getId(), dto.getContentType(), promptTokens,
                    completionTokens, System.currentTimeMillis() - startTime, "SUCCESS", null);

            return contentMapper.poToVo(po);

        } catch (Exception e) {
            log.error("Content generation failed for user {}, type {}", currentUser.getId(), dto.getContentType(), e);
            po.setStatus("FAILED");
            contentRepository.updateById(po);

            recordUsageStats(currentUser.getId(), dto.getContentType(), 0, 0,
                    System.currentTimeMillis() - startTime, "FAILED", e.getMessage());

            throw new BusinessException(500, "内容生成失败，请稍后重试");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GeneratedContentVO> listGeneratedContents(String contentType, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (contentType != null && !contentType.isBlank()) {
            IPage<GeneratedContentPO> poPage = contentRepository.findByUserIdAndContentTypeOrderByCreatedAtDesc(
                    MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId(), contentType.toUpperCase());
            return contentMapper.poPageToVoPage(
                    MybatisPlusPageUtils.toSpringPage(poPage, pageable));
        }
        IPage<GeneratedContentPO> poPage = contentRepository.findByUserIdOrderByCreatedAtDesc(
                MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId());
        return contentMapper.poPageToVoPage(
                MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public GeneratedContentVO getGeneratedContent(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        GeneratedContentPO po = contentRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "生成内容不存在");
        }
        if (!po.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "无权访问该内容");
        }
        return contentMapper.poToVo(po);
    }

    // ── Prompt templates ───────────────────────────────────────────────────────

    private String buildPrompt(GenerateContentDTO dto) {
        String requirements = dto.getRequirements() != null ? "\n额外要求：" + dto.getRequirements() : "";
        return switch (dto.getContentType().toUpperCase()) {
            case "OUTLINE" -> String.format(
                    "请为以下主题生成一份详细的复习提纲，包含主要知识点和子知识点，层次分明，条理清晰。\n主题：%s%s",
                    dto.getSubject(), requirements);
            case "EXAM_POINTS" -> String.format(
                    "请总结以下主题的重要考点，按重要性排序，并简要说明每个考点的核心内容。\n主题：%s%s",
                    dto.getSubject(), requirements);
            case "QUESTIONS" -> {
                int count = dto.getCount() != null ? dto.getCount() : 5;
                String difficulty = dto.getDifficulty() != null ? dto.getDifficulty() : "MEDIUM";
                yield String.format(
                        "请为以下主题生成 %d 道%s习题，包含题目和参考答案。题型可以包括选择题、填空题和简答题。\n主题：%s%s",
                        count, difficultyLabel(difficulty), dto.getSubject(), requirements);
            }
            case "BREAKDOWN" -> String.format(
                    "请对以下知识点进行详细拆解，包括定义、原理、关键要素、应用场景和常见误区。\n知识点：%s%s",
                    dto.getSubject(), requirements);
            case "LEARNING_PATH" -> String.format(
                    "请为零基础学习者生成阶梯式学习路径。\n主题：%s\n" +
                            "输出格式要求：\n" +
                            "1) 总学习目标\n" +
                            "2) 分阶段计划（入门/基础/进阶/冲刺），每阶段含目标、每日任务、验收方式\n" +
                            "3) 每周复盘建议\n" +
                            "4) 常见卡点与解决策略\n%s",
                    dto.getSubject(), requirements);
            case "EXPLANATION" -> String.format(
                    "请对以下题目进行详细解析。\n题目：%s\n" +
                            "输出要求：\n" +
                            "1) 题意理解\n" +
                            "2) 解题思路\n" +
                            "3) 易错点\n" +
                            "4) 举一反三练习建议\n%s",
                    dto.getSubject(), requirements);
            case "MNEMONIC" -> String.format(
                    "请为以下知识点生成便于背诵的口诀/记忆法。\n知识点：%s\n" +
                            "输出要求：\n" +
                            "1) 主口诀（简短押韵）\n" +
                            "2) 口诀拆解含义\n" +
                            "3) 使用场景\n" +
                            "4) 一句话速记版本\n%s",
                    dto.getSubject(), requirements);
            case "REPORT_FRAMEWORK" -> String.format(
                    "请为以下主题生成实验/作业报告框架。\n主题：%s\n" +
                            "输出要求：\n" +
                            "1) 报告目录结构（一级/二级标题）\n" +
                            "2) 每一节应写内容与建议篇幅\n" +
                            "3) 可复用模板段落\n" +
                            "4) 提交前检查清单\n%s",
                    dto.getSubject(), requirements);
            case "CORE_POINTS" -> String.format(
                    "请对以下主题做核心要点梳理。\n主题：%s\n" +
                            "输出要求：\n" +
                            "1) 必懂概念\n" +
                            "2) 关键结论\n" +
                            "3) 易错点与纠正\n" +
                            "4) 3分钟速览版\n%s",
                    dto.getSubject(), requirements);
            case "DATA_PROCESSING" -> String.format(
                    "请为以下任务给出数据处理思路建议。\n任务主题：%s\n" +
                            "输出要求：\n" +
                            "1) 数据清洗步骤\n" +
                            "2) 特征构造/指标设计建议\n" +
                            "3) 分析流程（含可视化建议）\n" +
                            "4) 结果验证与风险提示\n%s",
                    dto.getSubject(), requirements);
            default -> throw new BusinessException(400, "不支持的生成类型：" + dto.getContentType());
        };
    }

    private String difficultyLabel(String difficulty) {
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> "简单";
            case "HARD" -> "难";
            default -> "中等";
        };
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

    private List<ChatMessage> buildMessages(String prompt) {
        if (properties.getSystemPrompt() == null || properties.getSystemPrompt().isBlank()) {
            return List.of(UserMessage.from(prompt));
        }
        return List.of(
                SystemMessage.from(properties.getSystemPrompt()),
                UserMessage.from(prompt)
        );
    }

    private String buildCacheKey(GenerateContentDTO dto) {
        StringBuilder builder = new StringBuilder();
        builder.append(defaultIfBlank(dto.getContentType(), "UNKNOWN")).append(':');
        builder.append(defaultIfBlank(dto.getSubject(), "")).append(':');
        builder.append(defaultIfBlank(dto.getRequirements(), "")).append(':');
        builder.append(defaultIfBlank(dto.getDifficulty(), "")).append(':');
        builder.append(dto.getCount() == null ? "" : dto.getCount()).append(':');
        builder.append(dto.getCourseId() == null ? "" : dto.getCourseId());
        return aiResponseCache.buildKey(dto.getContentType(), builder.toString());
    }

    private String loadCachedContent(String cacheKey) {
        if (!properties.isEnableCache()) {
            return null;
        }
        return aiResponseCache.get(cacheKey);
    }

    private void cacheContent(String cacheKey, String content) {
        if (!properties.isEnableCache() || content == null || content.isBlank()) {
            return;
        }
        aiResponseCache.putWithTtlSeconds(cacheKey, content, properties.getCacheTtl());
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

    private String appendRecommendedResources(String content, Long courseId, String subject) {
        if (courseId == null) return content;
        List<com.coursebuddy.domain.po.KnowledgeItemPO> matched = knowledgeItemRepository
                .searchByCourseAndKeyword(courseId, subject == null ? "" : subject.trim());
        if (matched.isEmpty()) {
            matched = knowledgeItemRepository.findByCourseId(courseId);
        }
        List<com.coursebuddy.domain.po.KnowledgeItemPO> top = matched.stream().limit(5).toList();
        if (top.isEmpty()) return content;

        StringBuilder sb = new StringBuilder(content == null ? "" : content);
        sb.append("\n\n---\n");
        sb.append("### 对应资料关联推送\n");
        for (com.coursebuddy.domain.po.KnowledgeItemPO item : top) {
            sb.append("- [知识点#").append(item.getId()).append("] ")
                    .append(defaultIfBlank(item.getTitle(), "Untitled"))
                    .append("（")
                    .append(defaultIfBlank(item.getCategory(), "General"))
                    .append("，")
                    .append(defaultIfBlank(item.getSourceType(), "MANUAL").toUpperCase(Locale.ROOT))
                    .append("）\n");
        }
        return sb.toString();
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
