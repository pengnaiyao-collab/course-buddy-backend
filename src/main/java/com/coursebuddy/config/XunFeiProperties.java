package com.coursebuddy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "xunfei")
public class XunFeiProperties {

    private boolean enabled = true;

    /** 讯飞应用 ID */
    private String appId = "";

    /** 讯飞 API Key */
    private String apiKey = "";

    /** 讯飞 API Secret */
    private String apiSecret = "";

    /** WebSocket 接口地址 */
    private String endpoint = "wss://spark-api.xf-yun.com/v3.5/chat";

    /** 模型版本（domain 参数） */
    private String model = "generalv3.5";

    /** 最大生成 token 数 */
    private int maxTokens = 2048;

    /** 采样温度 0-1 */
    private double temperature = 0.7;

    /** top_k 参数 */
    private int topK = 4;

    /** 请求超时（毫秒） */
    private long timeout = 30000;

    /** 失败重试次数 */
    private int retryCount = 3;

    /** 每分钟最大请求数 */
    private int rateLimitPerMinute = 60;

    /** 缓存 TTL（秒） */
    private long cacheTtl = 3600;

    /** 是否启用响应缓存 */
    private boolean enableCache = true;

    /** 是否启用监控统计 */
    private boolean enableMonitoring = true;

    /** 是否启用错误日志 */
    private boolean enableErrorLogging = true;
}
