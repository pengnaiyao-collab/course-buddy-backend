package com.coursebuddy.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LongSummaryStatistics;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI 性能指标收集器
 */
@Slf4j
@Component
public class PerformanceMetrics {

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalTokens = new AtomicLong(0);
    private final CopyOnWriteArrayList<Long> latencies = new CopyOnWriteArrayList<>();

    private static final int MAX_LATENCY_SAMPLES = 1000;

    /** 记录一次成功请求 */
    public void recordSuccess(long durationMs, int tokens) {
        totalRequests.incrementAndGet();
        successRequests.incrementAndGet();
        totalTokens.addAndGet(tokens);
        addLatency(durationMs);
    }

    /** 记录一次失败请求 */
    public void recordFailure(long durationMs) {
        totalRequests.incrementAndGet();
        failedRequests.incrementAndGet();
        addLatency(durationMs);
    }

    private void addLatency(long durationMs) {
        // CopyOnWriteArrayList 适合低写高读场景；
        // 滑动窗口清理保持列表有界。
        while (latencies.size() >= MAX_LATENCY_SAMPLES) {
            latencies.remove(0);
        }
        latencies.add(durationMs);
    }

    /** 获取平均响应时间（ms） */
    public double avgLatencyMs() {
        if (latencies.isEmpty()) return 0.0;
        LongSummaryStatistics stats = latencies.stream()
                .mapToLong(Long::longValue).summaryStatistics();
        return stats.getAverage();
    }

    /** 获取 P95 响应时间（ms） */
    public long p95LatencyMs() {
        if (latencies.isEmpty()) return 0L;
        long[] sorted = latencies.stream().mapToLong(Long::longValue).sorted().toArray();
        int idx = (int) Math.ceil(sorted.length * 0.95) - 1;
        return sorted[Math.max(0, idx)];
    }

    /** 成功率 */
    public double successRate() {
        long total = totalRequests.get();
        return total == 0 ? 1.0 : (double) successRequests.get() / total;
    }

    public long getTotalRequests() { return totalRequests.get(); }
    public long getSuccessRequests() { return successRequests.get(); }
    public long getFailedRequests() { return failedRequests.get(); }
    public long getTotalTokens() { return totalTokens.get(); }

    /** 重置所有指标 */
    public void reset() {
        totalRequests.set(0);
        successRequests.set(0);
        failedRequests.set(0);
        totalTokens.set(0);
        latencies.clear();
        log.info("Performance metrics reset");
    }
}
