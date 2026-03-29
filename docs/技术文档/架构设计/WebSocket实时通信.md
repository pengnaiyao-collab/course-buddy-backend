# WebSocket 实时通信

## 1. 概述

Course Buddy Backend 使用 Spring WebSocket 实现两种实时通信场景：

1. **协作 WebSocket** (`/ws/collaboration/{projectId}`)：多用户实时协作编辑、任务同步
2. **AI WebSocket** (`/ws/ai`)：与讯飞星火 AI 进行流式对话

---

## 2. WebSocket 配置

### 2.1 WebSocket 配置类

```java
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final CollaborationWebSocketHandler collaborationHandler;
    private final AIWebSocketHandler aiHandler;
    private final JwtHandshakeInterceptor jwtInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 协作 WebSocket 端点
        registry.addHandler(collaborationHandler, "/ws/collaboration/{projectId}")
                .addInterceptors(jwtInterceptor)
                .setAllowedOriginPatterns("*");

        // AI 对话 WebSocket 端点
        registry.addHandler(aiHandler, "/ws/ai")
                .addInterceptors(jwtInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
```

### 2.2 JWT 握手拦截器

WebSocket 连接建立时通过 URL 查询参数传递 JWT Token（WebSocket 协议不支持自定义请求头）：

```java
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                    ServerHttpResponse response,
                                    WebSocketHandler wsHandler,
                                    Map<String, Object> attributes) {

        // 从 URL 查询参数获取 Token
        // 示例：ws://localhost:8080/api/ws/collaboration/123?token=eyJhbGc...
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest()
                                        .getParameter("token");
            if (token == null || token.isBlank()) {
                log.warn("WebSocket 握手失败：缺少 token");
                return false;
            }

            // 验证 Token
            if (!jwtUtil.validateToken(token)) {
                log.warn("WebSocket 握手失败：Token 无效");
                return false;
            }

            // 检查 Token 是否在黑名单
            String blacklistKey = "token:blacklist:" + token;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                log.warn("WebSocket 握手失败：Token 已失效");
                return false;
            }

            // 将用户信息存入 WebSocket Session 属性
            Long userId = jwtUtil.extractUserId(token);
            String username = jwtUtil.extractUsername(token);
            attributes.put("userId", userId);
            attributes.put("username", username);
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                                ServerHttpResponse response,
                                WebSocketHandler wsHandler,
                                Exception exception) {
        // 握手完成后的处理（通常无需操作）
    }
}
```

---

## 3. 协作 WebSocket Handler

### 3.1 连接管理

```java
@Component
@Slf4j
@RequiredArgsConstructor
public class CollaborationWebSocketHandler extends TextWebSocketHandler {

    private final CollaborationService collaborationService;

    // projectId → {userId → WebSocketSession} 映射
    private final Map<String, Map<Long, WebSocketSession>> projectSessions =
        new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String projectId = extractProjectId(session);
        Long userId = (Long) session.getAttributes().get("userId");
        String username = (String) session.getAttributes().get("username");

        // 注册连接
        projectSessions.computeIfAbsent(projectId, k -> new ConcurrentHashMap<>())
                       .put(userId, session);

        log.info("用户 [{}] 加入协作项目 [{}]，当前在线人数：{}",
            username, projectId, projectSessions.get(projectId).size());

        // 通知其他用户有人加入
        broadcastToProject(projectId, userId, CollaborationMessage.builder()
            .type(MessageType.USER_JOINED)
            .userId(userId)
            .username(username)
            .content("用户加入协作")
            .build());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                       CloseStatus status) {
        String projectId = extractProjectId(session);
        Long userId = (Long) session.getAttributes().get("userId");
        String username = (String) session.getAttributes().get("username");

        // 清理连接
        Map<Long, WebSocketSession> sessions = projectSessions.get(projectId);
        if (sessions != null) {
            sessions.remove(userId);
            if (sessions.isEmpty()) {
                projectSessions.remove(projectId);
            }
        }

        log.info("用户 [{}] 离开协作项目 [{}]", username, projectId);

        // 通知其他用户有人离开
        broadcastToProject(projectId, userId, CollaborationMessage.builder()
            .type(MessageType.USER_LEFT)
            .userId(userId)
            .username(username)
            .content("用户离开协作")
            .build());
    }
}
```

### 3.2 消息处理

```java
@Override
protected void handleTextMessage(WebSocketSession session,
                                  TextMessage message) throws Exception {
    Long userId = (Long) session.getAttributes().get("userId");
    String projectId = extractProjectId(session);

    // 解析消息
    CollaborationMessage msg = objectMapper.readValue(
        message.getPayload(), CollaborationMessage.class);
    msg.setUserId(userId);
    msg.setTimestamp(System.currentTimeMillis());

    switch (msg.getType()) {
        case CURSOR_MOVE -> handleCursorMove(projectId, userId, msg);
        case CONTENT_CHANGE -> handleContentChange(projectId, userId, msg);
        case TASK_UPDATE -> handleTaskUpdate(projectId, userId, msg);
        case PING -> session.sendMessage(new TextMessage(
            objectMapper.writeValueAsString(
                CollaborationMessage.builder().type(MessageType.PONG).build())));
        default -> log.warn("未知消息类型: {}", msg.getType());
    }
}

private void handleContentChange(String projectId, Long userId,
                                   CollaborationMessage msg) {
    // 持久化变更
    collaborationService.saveCollaborationLog(
        Long.parseLong(projectId), userId, msg.getContent());

    // 广播给项目内其他用户（排除发送者）
    broadcastToProject(projectId, userId, msg);
}
```

### 3.3 消息广播

```java
private void broadcastToProject(String projectId, Long excludeUserId,
                                  CollaborationMessage message) {
    Map<Long, WebSocketSession> sessions = projectSessions.get(projectId);
    if (sessions == null) return;

    String json;
    try {
        json = objectMapper.writeValueAsString(message);
    } catch (JsonProcessingException e) {
        log.error("消息序列化失败", e);
        return;
    }

    sessions.forEach((uid, session) -> {
        if (!uid.equals(excludeUserId) && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                log.warn("发送消息失败，用户: {}", uid, e);
            }
        }
    });
}

// 发送给特定用户
private void sendToUser(Long userId, String projectId,
                         CollaborationMessage message) {
    Map<Long, WebSocketSession> sessions = projectSessions.get(projectId);
    if (sessions == null) return;
    WebSocketSession session = sessions.get(userId);
    if (session != null && session.isOpen()) {
        try {
            session.sendMessage(new TextMessage(
                objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.warn("发送消息给用户 {} 失败", userId, e);
        }
    }
}
```

---

## 4. 讯飞星火 AI WebSocket 集成

### 4.1 AI WebSocket Handler（服务端）

```java
@Component
@Slf4j
@RequiredArgsConstructor
public class AIWebSocketHandler extends TextWebSocketHandler {

    private final XunFeiSparkService sparkService;

    // 每个客户端连接对应一个 AI 会话
    private final Map<String, WebSocketSession> clientSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        clientSessions.put(session.getId(), session);
        log.info("用户 [{}] 建立 AI 对话连接，sessionId: {}", userId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session,
                                      TextMessage message) throws Exception {
        AIRequestMessage request = objectMapper.readValue(
            message.getPayload(), AIRequestMessage.class);

        Long userId = (Long) session.getAttributes().get("userId");

        // 调用讯飞 Spark AI（流式返回）
        sparkService.askStream(
            request.getQuestion(),
            request.getSessionId(),
            // 流式回调：每收到一段 AI 回复就推送给客户端
            chunk -> {
                if (session.isOpen()) {
                    try {
                        AIResponseMessage response = AIResponseMessage.builder()
                            .type(chunk.isEnd() ? "END" : "CHUNK")
                            .content(chunk.getContent())
                            .sessionId(request.getSessionId())
                            .build();
                        session.sendMessage(new TextMessage(
                            objectMapper.writeValueAsString(response)));
                    } catch (IOException e) {
                        log.warn("AI 流式推送失败", e);
                    }
                }
            }
        );
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                       CloseStatus status) {
        clientSessions.remove(session.getId());
        log.info("AI 对话连接关闭，sessionId: {}", session.getId());
    }
}
```

### 4.2 讯飞 Spark AI 客户端（WebSocket 客户端）

Course Buddy 通过 Java WebSocket 客户端连接到讯飞 Spark AI 的 WebSocket 服务：

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class XunFeiSparkService {

    @Value("${xunfei.app-id}")
    private String appId;

    @Value("${xunfei.api-key}")
    private String apiKey;

    @Value("${xunfei.api-secret}")
    private String apiSecret;

    // 讯飞 Spark AI WebSocket 端点（按版本变化）
    private static final String SPARK_URL =
        "wss://spark-api.xf-yun.com/v3.5/chat";

    /**
     * 流式问答
     * @param question 用户问题
     * @param sessionId 对话 ID（用于多轮对话上下文）
     * @param callback 流式回调
     */
    public void askStream(String question, String sessionId,
                           Consumer<AIChunk> callback) {
        // 1. 生成鉴权 URL（HMAC-SHA256 签名）
        String authUrl = buildAuthUrl();

        // 2. 构建请求 JSON
        JSONObject requestBody = buildSparkRequest(question, sessionId);

        // 3. 建立 WebSocket 连接并发送消息
        SparkWebSocketClient client = new SparkWebSocketClient(
            new URI(authUrl), requestBody, callback);
        client.connect();
    }

    private String buildAuthUrl() {
        // 讯飞鉴权：date + host + path 进行 HMAC-SHA256 签名
        String date = DateTimeFormatter.RFC_1123_DATE_TIME
            .format(ZonedDateTime.now(ZoneOffset.UTC));
        String host = "spark-api.xf-yun.com";
        String path = "/v3.5/chat";

        String signatureOrigin = "host: " + host + "\n"
            + "date: " + date + "\n"
            + "GET " + path + " HTTP/1.1";

        // HMAC-SHA256 签名（省略具体实现）
        String signature = HmacUtils.hmacSha256Base64(apiSecret, signatureOrigin);
        String authorization = String.format(
            "api_key=\"%s\", algorithm=\"hmac-sha256\", "
            + "headers=\"host date request-line\", signature=\"%s\"",
            apiKey, signature);

        return SPARK_URL + "?" + URLEncoder.encode(
            "authorization=" + Base64.encode(authorization)
            + "&date=" + date + "&host=" + host, StandardCharsets.UTF_8);
    }
}
```

### 4.3 Spark AI 请求格式

```json
{
  "header": {
    "app_id": "your_app_id",
    "uid": "user_session_id"
  },
  "parameter": {
    "chat": {
      "domain": "generalv3.5",
      "temperature": 0.5,
      "max_tokens": 2048
    }
  },
  "payload": {
    "message": {
      "text": [
        {
          "role": "system",
          "content": "你是 Course Buddy 学习助手，帮助学生解答课程相关问题。"
        },
        {
          "role": "user",
          "content": "用户的问题内容"
        }
      ]
    }
  }
}
```

### 4.4 Spark AI 响应格式（流式）

```json
{
  "header": {
    "code": 0,
    "message": "Success",
    "sid": "cht000736d9@dx18abd9bc4c0b912532",
    "status": 1
  },
  "payload": {
    "choices": {
      "status": 1,
      "seq": 0,
      "text": [
        {
          "content": "这是 AI 回复的一段内容",
          "role": "assistant",
          "index": 0
        }
      ]
    }
  }
}
```

`status` 含义：
- `0` → 首帧
- `1` → 中间帧
- `2` → 末帧（对话结束）

---

## 5. 消息格式协议

### 5.1 协作 WebSocket 消息格式

**客户端 → 服务端**：
```json
{
  "type": "CONTENT_CHANGE",
  "content": "更新后的内容",
  "taskId": 42,
  "metadata": {
    "cursorPosition": 150,
    "changeType": "INSERT"
  }
}
```

**服务端 → 客户端**：
```json
{
  "type": "CONTENT_CHANGE",
  "userId": 1001,
  "username": "张三",
  "content": "更新后的内容",
  "taskId": 42,
  "timestamp": 1706150400000,
  "metadata": {
    "cursorPosition": 150,
    "changeType": "INSERT"
  }
}
```

### 5.2 消息类型枚举

| 类型 | 方向 | 说明 |
|------|------|------|
| `USER_JOINED` | 服务端→客户端 | 用户加入协作 |
| `USER_LEFT` | 服务端→客户端 | 用户离开协作 |
| `CURSOR_MOVE` | 双向 | 光标位置同步 |
| `CONTENT_CHANGE` | 双向 | 内容变更 |
| `TASK_UPDATE` | 双向 | 任务状态更新 |
| `PING` | 客户端→服务端 | 心跳检测 |
| `PONG` | 服务端→客户端 | 心跳响应 |

### 5.3 AI WebSocket 消息格式

**客户端 → 服务端（提问）**：
```json
{
  "question": "Java 中的多态是什么？",
  "sessionId": "session-uuid-1234"
}
```

**服务端 → 客户端（流式回复）**：
```json
{
  "type": "CHUNK",
  "content": "多态是面向对象编程的",
  "sessionId": "session-uuid-1234"
}
```

**服务端 → 客户端（结束标记）**：
```json
{
  "type": "END",
  "content": "",
  "sessionId": "session-uuid-1234"
}
```

---

## 6. 客户端连接示例

### 6.1 JavaScript 连接协作 WebSocket

```javascript
// 获取 JWT Token（登录后保存在 localStorage）
const token = localStorage.getItem('jwt_token');
const projectId = 42;

// 建立 WebSocket 连接（通过 query param 传递 Token）
const ws = new WebSocket(
  `ws://localhost:8080/api/ws/collaboration/${projectId}?token=${token}`
);

ws.onopen = () => {
  console.log('协作连接已建立');
  // 启动心跳
  setInterval(() => {
    ws.send(JSON.stringify({ type: 'PING' }));
  }, 30000);
};

ws.onmessage = (event) => {
  const message = JSON.parse(event.data);
  switch (message.type) {
    case 'USER_JOINED':
      console.log(`${message.username} 加入协作`);
      break;
    case 'CONTENT_CHANGE':
      // 更新本地编辑器内容
      updateEditorContent(message.content);
      break;
    case 'PONG':
      // 心跳响应
      break;
  }
};

ws.onerror = (error) => {
  console.error('WebSocket 错误', error);
};

ws.onclose = (event) => {
  console.log(`连接关闭，code: ${event.code}`);
  // 自动重连逻辑
  setTimeout(() => reconnect(), 3000);
};

// 发送内容变更
function sendContentChange(content) {
  ws.send(JSON.stringify({
    type: 'CONTENT_CHANGE',
    content: content
  }));
}
```

### 6.2 JavaScript 连接 AI WebSocket

```javascript
const token = localStorage.getItem('jwt_token');
const aiWs = new WebSocket(`ws://localhost:8080/api/ws/ai?token=${token}`);

let fullResponse = '';

aiWs.onmessage = (event) => {
  const response = JSON.parse(event.data);

  if (response.type === 'CHUNK') {
    fullResponse += response.content;
    // 实时显示流式输出
    document.getElementById('ai-response').textContent = fullResponse;
  } else if (response.type === 'END') {
    console.log('AI 回复完成');
    fullResponse = '';
  }
};

// 发送问题
function askAI(question) {
  fullResponse = '';
  aiWs.send(JSON.stringify({
    question: question,
    sessionId: 'session-' + Date.now()
  }));
}
```

---

## 7. 配置参数

### 7.1 application.yml 中的 WebSocket 相关配置

```yaml
# 讯飞星火 AI 配置
xunfei:
  app-id: ${XUNFEI_APP_ID}
  api-key: ${XUNFEI_API_KEY}
  api-secret: ${XUNFEI_API_SECRET}

# Spring WebSocket 缓冲区配置
spring:
  websocket:
    message-buffer-size: 65536
    send-buffer-size: 65536
    send-timeout: 5000

# Nginx WebSocket 代理配置（nginx.conf 中）
# proxy_http_version 1.1;
# proxy_set_header Upgrade $http_upgrade;
# proxy_set_header Connection "upgrade";
# proxy_read_timeout 3600s;
```

### 7.2 Nginx WebSocket 代理配置

```nginx
# nginx/conf.d/default.conf
location /api/ws/ {
    proxy_pass http://app:8080/api/ws/;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_read_timeout 3600s;  # 1小时超时（防止长连接被断开）
    proxy_send_timeout 3600s;
}
```

---

## 8. 注意事项与最佳实践

1. **Token 安全**：WebSocket 握手时通过 URL Query 参数传递 Token，注意 HTTPS 加密传输
2. **心跳机制**：客户端每 30 秒发送 PING，防止连接超时断开
3. **自动重连**：客户端应实现指数退避重连策略（1s → 2s → 4s → 最大 30s）
4. **并发安全**：`projectSessions` 使用 `ConcurrentHashMap` 确保线程安全
5. **资源清理**：连接断开时务必清理 `projectSessions` 中的 Session，防止内存泄漏
6. **消息大小限制**：默认最大消息 64KB，超大内容应分片发送
7. **多实例部署**：多个 Spring Boot 实例间的 WebSocket 消息需通过 Redis Pub/Sub 广播

---

## 9. 相关文档

- [系统架构概览](./系统架构概览.md)
- [环境变量配置](../../部署手册/环境变量配置.md)
- [常见错误及解决方案](../../运维文档/故障排查/常见错误及解决方案.md)
