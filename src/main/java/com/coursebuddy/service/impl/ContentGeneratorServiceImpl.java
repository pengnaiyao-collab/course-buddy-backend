package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.client.XunFeiSparkClient;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.config.XunFeiProperties;
import com.coursebuddy.domain.dto.GenerateContentDTO;
import com.coursebuddy.domain.po.AiUsageStatsPO;
import com.coursebuddy.domain.po.GeneratedContentPO;
import com.coursebuddy.domain.vo.GeneratedContentVO;
import com.coursebuddy.mapper.GeneratedContentMapper;
import com.coursebuddy.repository.AiUsageStatsRepository;
import com.coursebuddy.repository.GeneratedContentRepository;
import com.coursebuddy.service.IContentGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentGeneratorServiceImpl implements IContentGeneratorService {

    private final XunFeiSparkClient sparkClient;
    private final XunFeiProperties properties;
    private final GeneratedContentRepository contentRepository;
    private final AiUsageStatsRepository usageStatsRepository;
    private final GeneratedContentMapper contentMapper;

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
    public GeneratedContentVO generate(GenerateContentDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        long startTime = System.currentTimeMillis();

        String prompt = buildPrompt(dto);

        // Create pending record
        GeneratedContentPO po = GeneratedContentPO.builder()
                .userId(currentUser.getId())
                .contentType(dto.getContentType())
                .subject(dto.getSubject())
                .courseId(dto.getCourseId())
                .prompt(prompt)
                .status("PENDING")
                .build();
        po = contentRepository.save(po);

        try {
            List<Map<String, String>> messages = List.of(Map.of("role", "user", "content", prompt));
            XunFeiSparkClient.SparkChatResult result = sparkClient.chat(messages, String.valueOf(currentUser.getId()));

            po.setContent(result.content());
            po.setStatus("COMPLETED");
            po.setTokenCount(result.totalTokens());
            po = contentRepository.save(po);

            recordUsageStats(currentUser.getId(), dto.getContentType(), result.promptTokens(),
                    result.completionTokens(), System.currentTimeMillis() - startTime, "SUCCESS", null);

            return contentMapper.poToVo(po);

        } catch (Exception e) {
            log.error("Content generation failed for user {}, type {}", currentUser.getId(), dto.getContentType(), e);
            po.setStatus("FAILED");
            contentRepository.save(po);

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
            return contentMapper.poPageToVoPage(
                    contentRepository.findByUserIdAndContentTypeOrderByCreatedAtDesc(
                            currentUser.getId(), contentType.toUpperCase(), pageable));
        }
        return contentMapper.poPageToVoPage(
                contentRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public GeneratedContentVO getGeneratedContent(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        GeneratedContentPO po = contentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "生成内容不存在"));
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
