package com.coursebuddy.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI 响应缓存 - 简单的内存缓存，避免相同请求重复调用 AI
 */
@Slf4j
@Component
public class AIResponseCache {

    private static final long DEFAULT_TTL_MS = 3_600_000L; // 1 hour

    private record CacheEntry(String value, long expireAt) {
        boolean isExpired() {
            return System.currentTimeMillis() > expireAt;
        }
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);

    /** 生成缓存 key */
    public String buildKey(String contentType, String subject) {
        return contentType + ":" + subject.hashCode();
    }

    /** 获取缓存值，过期则返回 null */
    public String get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            if (entry != null) cache.remove(key);
            missCount.incrementAndGet();
            return null;
        }
        hitCount.incrementAndGet();
        log.debug("Cache hit for key: {}", key);
        return entry.value();
    }

    /** 写入缓存 */
    public void put(String key, String value) {
        cache.put(key, new CacheEntry(value, System.currentTimeMillis() + DEFAULT_TTL_MS));
        log.debug("Cached response for key: {}", key);
    }

    /** 写入缓存（自定义 TTL，单位秒 seconds） */
    public void putWithTtlSeconds(String key, String value, long ttlSeconds) {
        long ttlMs = ttlSeconds > 0 ? ttlSeconds * 1000L : DEFAULT_TTL_MS;
        cache.put(key, new CacheEntry(value, System.currentTimeMillis() + ttlMs));
        log.debug("Cached response for key: {}", key);
    }

    /** 删除指定 key */
    public void evict(String key) {
        cache.remove(key);
    }

    /** 清空所有缓存 */
    public void clear() {
        cache.clear();
        log.info("AI response cache cleared");
    }

    /** 清理过期条目 */
    public void evictExpired() {
        cache.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    /** 命中率统计 */
    public double hitRate() {
        long total = hitCount.get() + missCount.get();
        return total == 0 ? 0.0 : (double) hitCount.get() / total;
    }

    public long getHitCount() { return hitCount.get(); }
    public long getMissCount() { return missCount.get(); }
    public int size() { return cache.size(); }
}
