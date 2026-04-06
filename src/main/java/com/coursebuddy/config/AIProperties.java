package com.coursebuddy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AIProperties {

    private boolean enabled = true;
    private String provider = "openai-compatible";
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    private String apiKey = "";
    private String model = "qwen-plus";
    private String systemPrompt = "你是课伴的 AI 学习助教，请优先给出准确、结构化、适合教学场景的回答。";
    private int maxTokens = 2048;
    private double temperature = 0.7;
    private Double topP = 0.8;
    private long timeout = 30000;
    private int retryCount = 3;
    private int rateLimitPerMinute = 60;
    private long cacheTtl = 3600;
    private boolean enableCache = true;
    private boolean enableMonitoring = true;
    private boolean enableErrorLogging = true;
    private boolean logRequests = false;
    private boolean logResponses = false;
    private int maxContextMessages = 20;
    private int maxSourceItems = 3;
}
