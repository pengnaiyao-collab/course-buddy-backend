package com.coursebuddy.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Token 计算工具 - 估算 AI 请求的 token 消耗
 */
@Component
public class TokenCalculator {

    /** 中文字符每个约 1.5 tokens；英文单词约 1.3 tokens */
    private static final double CHINESE_TOKENS_PER_CHAR = 1.5;
    private static final double ENGLISH_TOKENS_PER_WORD = 1.3;
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4E00-\\u9FFF]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    /** 估算文本的 token 数 */
    public int estimate(String text) {
        if (text == null || text.isBlank()) return 0;
        long chineseCount = text.chars().filter(c -> c >= 0x4E00 && c <= 0x9FFF).count();
        String withoutChinese = CHINESE_PATTERN.matcher(text).replaceAll("").trim();
        long wordCount = withoutChinese.isBlank() ? 0 :
                (long) WHITESPACE_PATTERN.split(withoutChinese).length;
        return (int) (chineseCount * CHINESE_TOKENS_PER_CHAR + wordCount * ENGLISH_TOKENS_PER_WORD);
    }

    /** 估算一组消息的总 token 数 */
    public int estimateMessages(java.util.List<java.util.Map<String, String>> messages) {
        return messages.stream()
                .mapToInt(m -> estimate(m.getOrDefault("content", "")) + 4)
                .sum();
    }

    /** 根据 token 数估算成本（讯飞星火，单位：元） */
    public double estimateCost(int tokens) {
        return tokens / 1000.0 * 0.005;
    }

    /** 判断是否超出模型最大 token 限制 */
    public boolean exceedsLimit(String text, int maxTokens) {
        return estimate(text) > maxTokens;
    }
}
