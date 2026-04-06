package com.coursebuddy.config;

import com.coursebuddy.util.AIResponseCache;
import com.coursebuddy.util.PerformanceMetrics;
import com.coursebuddy.util.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 通用 AI 配置 - 注册 AI 相关工具 Bean 并配置定时清理任务
 */
@Slf4j
@Configuration
@EnableScheduling
public class AIConfig {

    private static final long CLEANUP_INTERVAL_MS = 3_600_000L; // 1 小时

    private final AIResponseCache aiResponseCache;
    private final RateLimiter rateLimiter;

    public AIConfig(AIResponseCache aiResponseCache, RateLimiter rateLimiter) {
        this.aiResponseCache = aiResponseCache;
        this.rateLimiter = rateLimiter;
    }

    /** 每小时清理过期缓存和速率限制器桶 */
    @Scheduled(fixedDelay = CLEANUP_INTERVAL_MS)
    public void cleanupExpiredEntries() {
        log.debug("AI 缓存与限流器定时清理执行中");
        aiResponseCache.evictExpired();
        rateLimiter.cleanup();
    }
}
