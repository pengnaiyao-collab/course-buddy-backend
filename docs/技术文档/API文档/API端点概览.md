# API 端点概览

本文档列出 课伴 Backend 的 REST API 端点，按模块分组。

> **基础信息**
> - Base URL（开发）：`http://localhost:8080/api`
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

---

## 认证模块（Authentication）

**Base Path**: `/v1/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/auth/register` | 注册新用户 |
| `POST` | `/v1/auth/login` | 用户登录 |
| `POST` | `/v1/auth/refresh-token` | 刷新 Token |
| `POST` | `/v1/auth/logout` | 登出 |
| `GET` | `/v1/auth/me` | 获取当前用户信息 |

---

## 用户资料模块（User Profile）

**Base Path**: `/users`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/users/me` | 获取我的资料 |
| `PUT` | `/users/me` | 更新我的资料 |
| `POST` | `/users/me/avatar` | 更新头像 |
| `GET` | `/users/{userId}/profile` | 获取用户资料 |
| `GET` | `/users/search` | 搜索用户 |
| `GET` | `/users/pending-teachers` | 获取待审核教师（管理员） |
| `POST` | `/users/{userId}/approve` | 通过教师审核（管理员） |
| `POST` | `/users/{userId}/reject` | 拒绝教师审核（管理员） |

---

## 课程目录模块（Course Catalog）

**Base Path**: `/v1/courses-catalog`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/courses-catalog` | 获取课程列表 |
| `GET` | `/v1/courses-catalog/search` | 搜索课程 |
| `GET` | `/v1/courses-catalog/{courseId}` | 获取课程详情 |
| `POST` | `/v1/courses-catalog` | 创建课程（教师） |
| `PUT` | `/v1/courses-catalog/{courseId}` | 更新课程（教师） |
| `DELETE` | `/v1/courses-catalog/{courseId}` | 删除课程（教师/管理员） |
| `GET` | `/v1/courses-catalog/my-teaching` | 我的授课课程（教师） |
| `GET` | `/v1/courses-catalog/my-enrolled` | 我的选课课程（学生） |
| `GET` | `/v1/courses-catalog/{courseId}/stats` | 课程统计（教师/助教） |

---

## 选课模块（Enrollments）

**Base Path**: `/enrollments`

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/enrollments` | 选课 |
| `POST` | `/enrollments/join` | 通过邀请码加入 |
| `PUT` | `/enrollments/courses/{courseId}/drop` | 退课 |
| `PUT` | `/enrollments/courses/{courseId}/complete` | 标记课程完成 |
| `GET` | `/enrollments` | 获取我的选课列表 |
| `GET` | `/enrollments/courses/{courseId}/students` | 获取课程学生列表 |
| `GET` | `/enrollments/courses/{courseId}/status` | 查询是否已选课 |

---

## 课节模块（Lessons）

**Base Path**: `/v1/lessons`

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/lessons/courses/{courseId}` | 创建课节 |
| `GET` | `/v1/lessons/courses/{courseId}` | 获取课程课节列表 |
| `GET` | `/v1/lessons/{lessonId}` | 获取课节详情 |
| `PUT` | `/v1/lessons/{lessonId}` | 更新课节 |
| `DELETE` | `/v1/lessons/{lessonId}` | 删除课节 |
| `POST` | `/v1/lessons/{lessonId}/publish` | 发布课节 |
| `POST` | `/v1/lessons/courses/{courseId}/reorder` | 调整课节顺序 |

---

## 课程资源模块（Course Resources）

**Base Path**: `/v1/course-resources`

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/course-resources/courses/{courseId}` | 创建课程资源（教师/助教） |
| `GET` | `/v1/course-resources/courses/{courseId}` | 获取课程资源列表 |
| `GET` | `/v1/course-resources/{resourceId}` | 获取资源详情 |
| `DELETE` | `/v1/course-resources/{resourceId}` | 删除资源（教师/助教） |

---

## 课程讨论模块（Discussions）

**Base Path**: `/discussions`

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/discussions` | 发布讨论 |
| `GET` | `/discussions/courses/{courseId}` | 获取课程讨论列表 |
| `GET` | `/discussions/{id}` | 获取讨论详情 |
| `PUT` | `/discussions/{id}` | 更新讨论 |
| `DELETE` | `/discussions/{id}` | 删除讨论 |
| `POST` | `/discussions/{id}/pin` | 置顶/取消置顶 |

---

## 笔记模块（Notes）

**Base Path**: `/notes`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/notes` | 获取我的笔记列表 |
| `POST` | `/notes` | 创建笔记 |
| `GET` | `/notes/{id}` | 获取笔记详情 |
| `PUT` | `/notes/{id}` | 更新笔记 |
| `DELETE` | `/notes/{id}` | 删除笔记 |

### 笔记版本

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/notes/{noteId}/versions` | 保存版本快照 |
| `GET` | `/notes/{noteId}/versions` | 获取版本列表 |
| `GET` | `/notes/{noteId}/versions/{versionNo}` | 获取指定版本 |
| `POST` | `/notes/{noteId}/versions/{versionNo}/restore` | 恢复到指定版本 |

---

## 作业模块（Assignments）

**Base Path**: `/v1/assignments`

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/assignments/courses/{courseId}` | 创建作业 |
| `GET` | `/v1/assignments/courses/{courseId}` | 获取课程作业列表 |
| `GET` | `/v1/assignments/{assignmentId}` | 获取作业详情 |
| `PUT` | `/v1/assignments/{assignmentId}` | 更新作业 |
| `DELETE` | `/v1/assignments/{assignmentId}` | 删除作业 |
| `POST` | `/v1/assignments/{assignmentId}/publish` | 发布作业 |

### 作业提交（Submissions）

**Base Path**: `/v1/submissions`

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/submissions/assignments/{assignmentId}` | 提交作业 |
| `GET` | `/v1/submissions/assignments/{assignmentId}` | 获取作业提交列表（教师/助教） |
| `GET` | `/v1/submissions/assignments/{assignmentId}/download` | 下载全部提交（教师/助教） |
| `GET` | `/v1/submissions/assignments/{assignmentId}/my` | 获取我的提交 |
| `GET` | `/v1/submissions/courses/{courseId}/my` | 获取课程我的提交 |
| `GET` | `/v1/submissions/courses/{courseId}/counts` | 获取提交数量（教师/助教） |
| `GET` | `/v1/submissions/{submissionId}` | 获取提交详情 |
| `PUT` | `/v1/submissions/{submissionId}` | 更新提交 |
| `POST` | `/v1/submissions/{submissionId}/grade` | 批改作业 |

---

## 考勤模块（Attendance）

**Base Path**: `/v1/attendance`

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/attendance/courses/{courseId}` | 手动登记出勤（教师/助教） |
| `GET` | `/v1/attendance/courses/{courseId}` | 获取课程考勤 |
| `GET` | `/v1/attendance/courses/{courseId}/date` | 按日期查询考勤 |
| `GET` | `/v1/attendance/courses/{courseId}/my` | 获取我的考勤 |
| `GET` | `/v1/attendance/courses/{courseId}/my/date` | 按日期查询我的考勤 |
| `GET` | `/v1/attendance/courses/{courseId}/my-stats` | 获取我的考勤统计 |
| `POST` | `/v1/attendance/courses/{courseId}/code` | 生成签到码（教师/助教） |
| `GET` | `/v1/attendance/courses/{courseId}/code` | 获取当前签到码（教师/助教） |
| `POST` | `/v1/attendance/courses/{courseId}/checkin` | 学生签到（Query: `code`） |

---

## 成绩模块（Grades）

**Base Path**: `/v1/grades`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/v1/grades/courses/{courseId}` | 获取课程成绩单（教师/助教） |
| `GET` | `/v1/grades/courses/{courseId}/my` | 获取我的成绩 |
| `PUT` | `/v1/grades/courses/{courseId}/students/{studentId}` | 更新学生成绩（教师/助教） |

---

## 学习进度模块（Learning Progress）

**Base Path**: `/learning-progress`

| 方法 | 路径 | 说明 |
|------|------|------|
| `PUT` | `/learning-progress` | 更新学习进度 |
| `GET` | `/learning-progress` | 获取我的学习进度列表 |
| `GET` | `/learning-progress/courses/{courseId}` | 获取课程学习进度 |
| `GET` | `/learning-progress/courses/{courseId}/stats` | 获取课程学习统计 |
| `GET` | `/learning-progress/courses/{courseId}/average` | 获取课程平均进度 |

---

## 文件上传模块（Files）

**Base Path**: `/v1/files`

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/files/upload/init` | 初始化分块上传 |
| `POST` | `/v1/files/upload/chunk` | 上传分片 |
| `POST` | `/v1/files/upload/merge` | 合并分片 |
| `GET` | `/v1/files/upload/progress/{sessionId}` | 查询上传进度 |
| `POST` | `/v1/files/upload/cancel/{sessionId}` | 取消上传 |
| `POST` | `/v1/files/upload/batch` | 批量上传 |
| `GET` | `/v1/files/download` | 下载文件（Query: `objectName`） |
| `GET` | `/v1/files/preview` | 获取预览 URL |
| `GET` | `/v1/files/avatar` | 预览头像 |
| `DELETE` | `/v1/files` | 删除文件（Query: `objectName`） |

---

## AI 助手模块（AI Chat）

**Base Path**: `/v1/ai`

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/ai/chat` | 同步对话 |
| `POST` | `/v1/ai/chat/stream` | 流式对话（SSE） |
| `GET` | `/v1/ai/conversations` | 获取对话列表 |
| `GET` | `/v1/ai/conversations/{id}/messages` | 获取对话消息 |
| `PUT` | `/v1/ai/conversations/{id}/archive` | 归档对话 |
| `DELETE` | `/v1/ai/conversations/{id}` | 删除对话 |

---

## 内容审核模块（Content Review）

**Base Path**: `/v1/reviews`（管理员）

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/reviews/submit` | 提交审核 |
| `GET` | `/v1/reviews/pending` | 获取待审核列表 |
| `GET` | `/v1/reviews/{reviewId}` | 获取审核详情 |
| `POST` | `/v1/reviews/{reviewId}/approve` | 审核通过 |
| `POST` | `/v1/reviews/{reviewId}/reject` | 审核拒绝 |
| `POST` | `/v1/reviews/{reviewId}/violation/takedown` | 违规下架 |
| `POST` | `/v1/reviews/{reviewId}/violation/remove` | 违规删除 |

---

## 版本管理模块（Versions）

**Base Path**: `/v1/versions`

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/v1/versions` | 保存版本 |
| `GET` | `/v1/versions` | 获取版本列表 |
| `GET` | `/v1/versions/{versionNumber}` | 获取指定版本 |
| `GET` | `/v1/versions/compare` | 对比版本 |

---

## 统计模块（Statistics）

**Base Path**: `/statistics`

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/statistics/{courseId}/score` | 课程成绩统计（教师/助教） |

---

## 系统接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| `GET` | `/actuator/health` | 健康检查 | 公开 |
| `GET` | `/v3/api-docs` | OpenAPI 规范 JSON | 公开 |
| `GET` | `/swagger-ui.html` | Swagger UI | 公开 |

---

## 相关文档

- [认证接口详细文档](./认证接口文档.md)
- [错误码参考](./错误码参考.md)
- [Swagger UI 使用说明](./Swagger配置说明.md)
