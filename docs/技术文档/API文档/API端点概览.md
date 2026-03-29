# API 端点概览

本文档列出 Course Buddy Backend 的所有 REST API 端点，按模块分组。

> **基础信息**
> - Base URL（开发）：`http://localhost:8080/api`
> - API 版本前缀：`/v1/`
> - 认证方式：`Authorization: Bearer <JWT Token>`
> - 响应格式：`application/json`

---

## 统一响应格式

所有接口均返回统一的 `ApiResponse<T>` 格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | 业务状态码（200 成功，4xx 客户端错误，5xx 服务器错误） |
| `message` | string | 描述信息 |
| `data` | any | 响应数据，错误时为 null |

---

## 认证模块（Authentication）

**Base Path**: `/v1/auth`  **认证要求**: 公开（不需要 Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/auth/register` | 注册新用户 |
| `POST` | `/v1/auth/login` | 用户登录，返回 JWT Token |

详细文档：[认证接口文档](./认证接口文档.md)

---

## 用户档案模块（User Profile）

**Base Path**: `/v1/user-profile`  **认证要求**: 需要 Token

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/user-profile` | 获取当前用户个人信息 |
| `PUT` | `/v1/user-profile` | 更新个人信息 |
| `PUT` | `/v1/user-profile/avatar` | 更新头像 |
| `PUT` | `/v1/user-profile/password` | 修改密码 |

---

## 课程管理模块（Course）

**Base Path**: `/v1/courses`  **认证要求**: 需要 Token

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/courses` | 获取课程列表（分页） |
| `POST` | `/v1/courses` | 创建新课程 |
| `GET` | `/v1/courses/{id}` | 获取课程详情 |
| `PUT` | `/v1/courses/{id}` | 更新课程信息 |
| `DELETE` | `/v1/courses/{id}` | 删除课程 |
| `GET` | `/v1/courses/{id}/members` | 获取课程成员 |
| `POST` | `/v1/courses/{id}/members` | 添加课程成员 |

### 课程目录（Course Catalog）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/courses/{courseId}/catalog` | 获取课程目录结构 |
| `POST` | `/v1/courses/{courseId}/catalog` | 添加目录节点 |
| `PUT` | `/v1/courses/{courseId}/catalog/{nodeId}` | 更新目录节点 |
| `DELETE` | `/v1/courses/{courseId}/catalog/{nodeId}` | 删除目录节点 |

### 课程权限（Course Permission）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/courses/{courseId}/permissions` | 获取课程权限配置 |
| `PUT` | `/v1/courses/{courseId}/permissions` | 更新课程权限 |

### 课程资源（Course Resource）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/courses/{courseId}/resources` | 获取课程资源列表 |
| `POST` | `/v1/courses/{courseId}/resources` | 上传课程资源 |
| `DELETE` | `/v1/courses/{courseId}/resources/{resourceId}` | 删除课程资源 |

### 课程讨论（Course Discussion）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/courses/{courseId}/discussions` | 获取课程讨论列表 |
| `POST` | `/v1/courses/{courseId}/discussions` | 发起新讨论 |
| `GET` | `/v1/courses/{courseId}/discussions/{discussionId}` | 获取讨论详情及回复 |
| `POST` | `/v1/courses/{courseId}/discussions/{discussionId}/replies` | 回复讨论 |

### 课程选课（Course Enrollment）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/enrollments` | 获取当前用户的选课列表 |
| `POST` | `/v1/enrollments` | 选课 |
| `DELETE` | `/v1/enrollments/{courseId}` | 退课 |

---

## 课时管理模块（Lesson）

**Base Path**: `/v1/lessons`  **认证要求**: 需要 Token

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/courses/{courseId}/lessons` | 获取课程的课时列表 |
| `POST` | `/v1/courses/{courseId}/lessons` | 创建新课时 |
| `GET` | `/v1/lessons/{lessonId}` | 获取课时详情 |
| `PUT` | `/v1/lessons/{lessonId}` | 更新课时 |
| `DELETE` | `/v1/lessons/{lessonId}` | 删除课时 |

---

## 笔记系统模块（Note）

**Base Path**: `/v1/notes`  **认证要求**: 需要 Token

### 笔记 CRUD

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/notes` | 获取笔记列表（分页，支持搜索） |
| `POST` | `/v1/notes` | 创建新笔记 |
| `GET` | `/v1/notes/{id}` | 获取笔记详情 |
| `PUT` | `/v1/notes/{id}` | 更新笔记内容 |
| `DELETE` | `/v1/notes/{id}` | 删除笔记 |

### 笔记版本历史

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/notes/{noteId}/versions` | 获取笔记版本列表 |
| `GET` | `/v1/notes/{noteId}/versions/{versionId}` | 获取某版本内容 |
| `POST` | `/v1/notes/{noteId}/versions/{versionId}/restore` | 恢复到指定版本 |

### 笔记分类

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/note-categories` | 获取笔记分类列表 |
| `POST` | `/v1/note-categories` | 创建分类 |
| `PUT` | `/v1/note-categories/{id}` | 更新分类 |
| `DELETE` | `/v1/note-categories/{id}` | 删除分类 |

### 笔记标签

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/note-tags` | 获取标签列表 |
| `POST` | `/v1/note-tags` | 创建标签 |
| `DELETE` | `/v1/note-tags/{id}` | 删除标签 |
| `POST` | `/v1/notes/{noteId}/tags` | 为笔记添加标签 |
| `DELETE` | `/v1/notes/{noteId}/tags/{tagId}` | 移除笔记标签 |

### 笔记分享

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/notes/{noteId}/share` | 生成笔记分享链接 |
| `GET` | `/v1/notes/shared/{shareToken}` | 通过分享链接访问笔记（公开） |
| `DELETE` | `/v1/notes/{noteId}/share` | 撤销分享 |

### 笔记导出

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/notes/{noteId}/export/pdf` | 导出为 PDF |
| `GET` | `/v1/notes/{noteId}/export/markdown` | 导出为 Markdown |
| `GET` | `/v1/notes/{noteId}/export/word` | 导出为 Word（.docx） |

### 网页导入

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/notes/import/web` | 从 URL 导入网页内容为笔记 |

---

## 实时协作模块（Collaboration）

**Base Path**: `/v1/collaboration`  **认证要求**: 需要 Token

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/collaboration/sessions` | 获取协作会话列表 |
| `POST` | `/v1/collaboration/sessions` | 创建协作会话 |
| `GET` | `/v1/collaboration/sessions/{sessionId}` | 获取会话详情 |
| `POST` | `/v1/collaboration/sessions/{sessionId}/join` | 加入协作会话 |
| `POST` | `/v1/collaboration/sessions/{sessionId}/leave` | 离开协作会话 |

**WebSocket 端点**: `ws://localhost:8080/api/ws/collaboration/{sessionId}`

---

## 知识库模块（Knowledge Base）

**Base Path**: `/v1/knowledge-base`  **认证要求**: 需要 Token

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/knowledge-base` | 获取知识库列表 |
| `POST` | `/v1/knowledge-base` | 创建知识点 |
| `GET` | `/v1/knowledge-base/{id}` | 获取知识点详情 |
| `PUT` | `/v1/knowledge-base/{id}` | 更新知识点 |
| `DELETE` | `/v1/knowledge-base/{id}` | 删除知识点 |

### 知识关联

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/knowledge-base/{id}/associations` | 获取知识点关联 |
| `POST` | `/v1/knowledge-base/{id}/associations` | 添加关联 |
| `DELETE` | `/v1/knowledge-base/{id}/associations/{targetId}` | 删除关联 |

---

## 作业管理模块（Assignment）

**Base Path**: `/v1/assignments`  **认证要求**: 需要 Token

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/courses/{courseId}/assignments` | 获取课程作业列表 |
| `POST` | `/v1/courses/{courseId}/assignments` | 发布新作业 |
| `GET` | `/v1/assignments/{id}` | 获取作业详情 |
| `PUT` | `/v1/assignments/{id}` | 更新作业 |
| `DELETE` | `/v1/assignments/{id}` | 删除作业 |

### 作业提交

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/assignments/{assignmentId}/submissions` | 提交作业 |
| `GET` | `/v1/assignments/{assignmentId}/submissions` | 查看所有提交（教师） |
| `GET` | `/v1/assignments/{assignmentId}/submissions/my` | 查看自己的提交 |
| `PUT` | `/v1/assignments/{assignmentId}/submissions/{submissionId}/grade` | 批改作业 |

---

## 考勤管理模块（Attendance）

**Base Path**: `/v1/attendance`  **认证要求**: 需要 Token

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/attendance/check-in` | 签到 |
| `GET` | `/v1/attendance/{courseId}/records` | 获取考勤记录 |
| `GET` | `/v1/attendance/{courseId}/statistics` | 获取考勤统计 |

---

## 团队管理模块（Team）

**Base Path**: `/v1/teams`  **认证要求**: 需要 Token

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/teams` | 获取团队列表 |
| `POST` | `/v1/teams` | 创建团队 |
| `GET` | `/v1/teams/{id}` | 获取团队详情 |
| `PUT` | `/v1/teams/{id}` | 更新团队信息 |
| `DELETE` | `/v1/teams/{id}` | 解散团队 |
| `POST` | `/v1/teams/{id}/members` | 邀请成员 |
| `DELETE` | `/v1/teams/{id}/members/{userId}` | 移除成员 |

---

## 文件上传模块（MinIO File Upload）

**Base Path**: `/v1/files`  **认证要求**: 需要 Token

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/files/upload` | 普通文件上传（≤20MB） |
| `POST` | `/v1/files/upload/init` | 初始化分块上传（大文件） |
| `POST` | `/v1/files/upload/chunk` | 上传分块 |
| `POST` | `/v1/files/upload/complete` | 合并完成分块上传 |
| `DELETE` | `/v1/files/upload/abort` | 取消分块上传 |
| `GET` | `/v1/files/{fileKey}` | 获取文件访问 URL |
| `DELETE` | `/v1/files/{fileKey}` | 删除文件 |

**文件大小限制**：
- 普通上传：20MB（`spring.servlet.multipart.max-file-size`）
- 分块上传：最大 1GB（`minio.maxFileSize`），每块 5MB（`minio.chunkSize`）

---

## AI 助手模块（XunFei Spark）

**Base Path**: `/v1/xunfei`  **认证要求**: 需要 Token

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/xunfei/chat` | 向讯飞星火发送对话请求 |
| `POST` | `/v1/xunfei/generate` | 使用 AI 生成内容 |
| `GET` | `/v1/xunfei/history` | 获取对话历史 |

### AI 辅助功能

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/ai/summarize` | AI 笔记摘要 |
| `POST` | `/v1/ai/suggest` | AI 内容建议 |

---

## OCR 识别模块

**Base Path**: `/v1/ocr`  **认证要求**: 需要 Token

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/ocr/recognize` | 上传图片进行文字识别 |

---

## 消息通知模块（Message）

**Base Path**: `/v1/messages`  **认证要求**: 需要 Token

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/messages` | 获取消息列表 |
| `PUT` | `/v1/messages/{id}/read` | 标记消息已读 |
| `PUT` | `/v1/messages/read-all` | 全部标记已读 |
| `DELETE` | `/v1/messages/{id}` | 删除消息 |

---

## 成绩单模块（Grade Sheet）

**Base Path**: `/v1/grade-sheets`  **认证要求**: 需要 Token

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/courses/{courseId}/grade-sheet` | 获取课程成绩单 |
| `PUT` | `/v1/courses/{courseId}/grade-sheet` | 更新成绩 |
| `GET` | `/v1/grade-sheets/my` | 获取个人成绩 |

---

## 学习进度模块（Learning Progress）

**Base Path**: `/v1/learning-progress`  **认证要求**: 需要 Token

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/learning-progress/{courseId}` | 获取课程学习进度 |
| `PUT` | `/v1/learning-progress/{lessonId}` | 更新课时学习状态 |

---

## 内容审核模块（Content Review）

**Base Path**: `/v1/content-review`  **认证要求**: 需要管理员权限

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/content-review/pending` | 获取待审核内容列表 |
| `POST` | `/v1/content-review/{id}/approve` | 审核通过 |
| `POST` | `/v1/content-review/{id}/reject` | 审核拒绝 |

---

## 系统接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| `GET` | `/actuator/health` | 健康检查 | 公开 |
| `GET` | `/v3/api-docs` | OpenAPI 规范 JSON | 公开 |
| `GET` | `/swagger-ui.html` | Swagger UI | 公开 |

---

## 分页参数规范

所有列表接口均支持以下分页参数：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `page` | int | 1 | 页码（从 1 开始） |
| `pageSize` | int | 10 | 每页条数（最大 100） |
| `keyword` | string | — | 搜索关键词（部分接口支持） |
| `sortBy` | string | `createTime` | 排序字段 |
| `sortOrder` | string | `desc` | 排序方向（`asc`/`desc`） |

**分页响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "page": 1,
    "pageSize": 10,
    "pages": 10
  }
}
```

---

## 相关文档

- [认证接口详细文档](./认证接口文档.md)
- [错误码参考](./错误码参考.md)
- [Swagger UI 使用说明](./Swagger配置说明.md)
