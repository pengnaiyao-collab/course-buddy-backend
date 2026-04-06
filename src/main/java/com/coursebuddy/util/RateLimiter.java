package com.coursebuddy.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI 接口速率限制器 - 滑动窗口算法，防止超出配额
 */
@Slf4j
@Component
public class RateLimiter {

    private static final long WINDOW_MS = 60_000L; // 1 分钟窗口

    private record UserBucket(AtomicInteger count, long windowStart) {}

    private final Map<Long, UserBucket> buckets = new ConcurrentHashMap<>();

    /**
     * 尝试获取令牌
     *
     * @param userId       用户 ID
     * @param limitPerMin  每分钟最大请求次数
     * @return true 表示允许通过，false 表示已超限
     */
    public boolean tryAcquire(Long userId, int limitPerMin) {
        long now = Instant.now().toEpochMilli();
        
        UserBucket bucket = buckets.compute(userId, (id, existing) -> {
            if (existing == null || now - existing.windowStart() >= WINDOW_MS) {
                AtomicInteger counter = new AtomicInteger(1);
                return new UserBucket(counter, now);
            }
            
            int current = existing.count().incrementAndGet();
            if (current > limitPerMin) {
                existing.count().decrementAndGet();
                log.warn("Rate limit exceeded for user {}: {}/{} req/min", userId, current - 1, limitPerMin);
                return null; // 标记为失败
            }
            return existing;
        });
        
        return bucket != null;
    }

    /** 获取用户当前窗口内的请求次数 */
    public int getCurrentCount(Long userId) {
        UserBucket bucket = buckets.get(userId);
        if (bucket == null) return 0;
        long now = Instant.now().toEpochMilli();
        if (now - bucket.windowStart() >= WINDOW_MS) return 0;
        return bucket.count().get();
    }

    /** 清理过期的桶（可由定时任务调用） */
    public void cleanup() {
        long now = Instant.now().toEpochMilli();
        buckets.entrySet().removeIf(e -> now - e.getValue().windowStart() >= WINDOW_MS);
    }
}
