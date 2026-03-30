package com.coursebuddy.util;

import org.springframework.stereotype.Component;

/**
 * AI 使用成本计算器
 */
@Component
public class CostCalculator {

    /** 通用模型基础价格（元/1K tokens） */
    private static final double AI_PRICE_PER_1K = 0.005;

    /** 计算单次请求成本（元） */
    public double calculate(int promptTokens, int completionTokens) {
        return (promptTokens + completionTokens) / 1000.0 * AI_PRICE_PER_1K;
    }

    /** 计算月度成本估算（元） */
    public double monthlyEstimate(long dailyTokens) {
        long freeQuota = 1_000_000L;
        long monthlyTokens = dailyTokens * 30;
        long billable = Math.max(0, monthlyTokens - freeQuota);
        return billable / 1000.0 * AI_PRICE_PER_1K;
    }

    /** 格式化成本字符串 */
    public String format(double cost) {
        return String.format("¥%.4f", cost);
    }
}
