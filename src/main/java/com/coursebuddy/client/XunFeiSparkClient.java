package com.coursebuddy.client;

import com.coursebuddy.config.XunFeiProperties;
import com.coursebuddy.util.XunFeiAuthUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 讯飞星火大模型 WebSocket 客户端
 * 支持同步对话和流式对话
 */
@Slf4j
public class XunFeiSparkClient {

    private static final int STATUS_LAST = 2;

    private final XunFeiProperties properties;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public XunFeiSparkClient(XunFeiProperties properties) {
        this.properties = properties;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(properties.getTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(properties.getTimeout(), TimeUnit.MILLISECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 同步对话：发送消息并等待完整响应
     *
     * @param messages 对话消息列表，每条消息包含 role 和 content
     * @param userId   用户标识
     * @return 包含回复文本和 token 统计的结果对象
     */
    public SparkChatResult chat(List<Map<String, String>> messages, String userId) {
        CompletableFuture<SparkChatResult> future = new CompletableFuture<>();

        StringBuilder responseBuilder = new StringBuilder();
        chatStream(messages, userId, new SparkStreamListener() {
            @Override
            public void onToken(String token) {
                responseBuilder.append(token);
            }

            @Override
            public void onComplete(int promptTokens, int completionTokens) {
                future.complete(new SparkChatResult(responseBuilder.toString(), promptTokens, completionTokens));
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }
        });

        try {
            return future.get(properties.getTimeout(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("XunFei Spark synchronous chat failed", e);
            throw new RuntimeException("XunFei Spark chat request failed: " + e.getMessage(), e);
        }
    }

    /** 同步对话结果，包含回复文本和 token 使用量 */
    public record SparkChatResult(String content, int promptTokens, int completionTokens) {
        public int totalTokens() {
            return promptTokens + completionTokens;
        }
    }

    /**
     * 流式对话：通过回调逐块接收响应
     *
     * @param messages 对话消息列表
     * @param userId   用户标识
     * @param listener 流式响应监听器
     */
    public void chatStream(List<Map<String, String>> messages, String userId, SparkStreamListener listener) {
        String authUrl = XunFeiAuthUtil.buildAuthUrl(
                properties.getEndpoint(), properties.getApiKey(), properties.getApiSecret());
        String requestBody = buildRequestBody(messages, userId);

        Request request = new Request.Builder().url(authUrl).build();
        httpClient.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                log.debug("XunFei Spark WebSocket connected, sending request");
                webSocket.send(requestBody);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JsonNode root = objectMapper.readTree(text);
                    int code = root.path("header").path("code").asInt(-1);
                    if (code != 0) {
                        String errMsg = root.path("header").path("message").asText("Unknown error");
                        log.error("XunFei Spark API error: code={}, message={}", code, errMsg);
                        listener.onError(new RuntimeException("XunFei API error " + code + ": " + errMsg));
                        webSocket.close(1000, "error");
                        return;
                    }

                    int status = root.path("payload").path("choices").path("status").asInt(0);
                    JsonNode textArr = root.path("payload").path("choices").path("text");
                    if (textArr.isArray()) {
                        for (JsonNode item : textArr) {
                            String content = item.path("content").asText("");
                            if (!content.isEmpty()) {
                                listener.onToken(content);
                            }
                        }
                    }

                    if (status == STATUS_LAST) {
                        int promptTokens = root.path("payload").path("usage")
                                .path("text").path("prompt_tokens").asInt(0);
                        int completionTokens = root.path("payload").path("usage")
                                .path("text").path("completion_tokens").asInt(0);
                        listener.onComplete(promptTokens, completionTokens);
                        webSocket.close(1000, "done");
                    } else {
                        webSocket.request();
                    }
                } catch (Exception e) {
                    log.error("Failed to parse XunFei Spark response", e);
                    listener.onError(e);
                    webSocket.close(1000, "parse error");
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                onMessage(webSocket, bytes.utf8());
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                log.error("XunFei Spark WebSocket failure", t);
                listener.onError(t);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                log.debug("XunFei Spark WebSocket closed: code={}, reason={}", code, reason);
            }
        });
    }

    private String buildRequestBody(List<Map<String, String>> messages, String userId) {
        try {
            ObjectNode root = objectMapper.createObjectNode();

            // header
            ObjectNode header = root.putObject("header");
            header.put("app_id", properties.getAppId());
            header.put("uid", userId != null ? userId : UUID.randomUUID().toString().substring(0, 8));

            // parameter
            ObjectNode parameter = root.putObject("parameter");
            ObjectNode chat = parameter.putObject("chat");
            chat.put("domain", properties.getModel());
            chat.put("temperature", properties.getTemperature());
            chat.put("max_tokens", properties.getMaxTokens());
            chat.put("top_k", properties.getTopK());
            chat.put("auditing", "default");

            // payload
            ObjectNode payload = root.putObject("payload");
            ObjectNode message = payload.putObject("message");
            ArrayNode textArray = message.putArray("text");
            for (Map<String, String> msg : messages) {
                ObjectNode msgNode = textArray.addObject();
                msgNode.put("role", msg.getOrDefault("role", "user"));
                msgNode.put("content", msg.getOrDefault("content", ""));
            }

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build XunFei request body", e);
        }
    }

    /**
     * 流式响应监听器接口
     */
    public interface SparkStreamListener {
        /** 收到一个 token 片段 */
        void onToken(String token);

        /** 响应接收完毕 */
        void onComplete(int promptTokens, int completionTokens);

        /** 发生错误 */
        void onError(Throwable t);
    }
}
