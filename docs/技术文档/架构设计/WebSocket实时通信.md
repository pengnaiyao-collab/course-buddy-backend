# 实时通信说明

当前版本不再使用 WebSocket 实现实时协作或 AI 对话。

实时输出仅用于 AI 对话场景，采用 **SSE（Server-Sent Events）** 实现：

- **接口**：`POST /api/v1/ai/chat/stream`
- **协议**：HTTP + `text/event-stream`
- **客户端示例**：

```bash
curl -N -X POST http://localhost:8080/api/v1/ai/chat/stream \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"message":"请解释 Java 中的多态性","courseId":1}'
```

如需实时协作功能，请在产品规划或后续版本中引入对应模块并补充相关文档。
