# REST API 设计规范

本文档定义 Course Buddy Backend 的 RESTful API 设计标准，确保 API 风格一致、语义清晰、易于使用。

---

## 基本原则

1. **资源导向**：URL 代表资源，HTTP 动词代表操作
2. **一致性**：整个 API 遵循统一的命名和行为规范
3. **无状态**：每个请求携带完整信息，服务器不保存客户端状态
4. **版本化**：API 从一开始就做好版本管理

---

## URL 设计规范

### 基础结构

```
http://localhost:8080/api/v1/{resource}
```

| 部分 | 说明 | 示例 |
|------|------|------|
| `/api` | Context Path（服务器统一前缀） | 固定值 |
| `/v1` | API 版本号 | `v1`、`v2` |
| `/{resource}` | 资源名称（复数名词） | `/notes`、`/courses` |

### URL 命名规则

**使用复数名词，小写，单词间用连字符 `-` 分隔：**

```
# ✅ 正确
GET  /v1/notes
GET  /v1/courses
GET  /v1/user-profile
GET  /v1/course-discussions
GET  /v1/note-categories
GET  /v1/knowledge-base

# ❌ 错误
GET  /v1/getNote         # 动词
GET  /v1/Note            # 大写
GET  /v1/note            # 单数
GET  /v1/course_list     # 下划线
GET  /v1/getNoteList     # 驼峰
```

### 资源层级关系

通过 URL 路径表达资源的从属关系（层级不超过 3 级）：

```
# 课程下的作业列表
GET    /v1/courses/{courseId}/assignments

# 作业的提交列表
GET    /v1/assignments/{assignmentId}/submissions

# 笔记的版本历史
GET    /v1/notes/{noteId}/versions

# ❌ 避免过深的嵌套（不超过 3 级）
GET  /v1/courses/{courseId}/modules/{moduleId}/lessons/{lessonId}/materials
# 可以改为：
GET  /v1/lessons/{lessonId}/materials
```

---

## HTTP 方法使用规范

| 方法 | 含义 | 幂等性 | 请求体 |
|------|------|--------|--------|
| `GET` | 查询资源 | ✅ 是 | 无 |
| `POST` | 创建资源 | ❌ 否 | JSON |
| `PUT` | 全量更新资源 | ✅ 是 | JSON |
| `PATCH` | 部分更新资源 | ❌ 否 | JSON（部分字段） |
| `DELETE` | 删除资源 | ✅ 是 | 无 |

### 标准 CRUD 映射

```
# 笔记资源的标准 CRUD

GET     /v1/notes              # 查询列表（分页）
POST    /v1/notes              # 创建笔记
GET     /v1/notes/{id}         # 查询单个笔记
PUT     /v1/notes/{id}         # 更新笔记（全量）
DELETE  /v1/notes/{id}         # 删除笔记
```

### 非 CRUD 操作

对于不符合 CRUD 模式的操作，使用动词作为资源的子资源：

```
# 加入/离开课程
POST    /v1/courses/{courseId}/join
POST    /v1/courses/{courseId}/leave

# 恢复笔记版本
POST    /v1/notes/{noteId}/versions/{versionId}/restore

# 提交作业
POST    /v1/assignments/{assignmentId}/submit

# 批量标记消息已读
PUT     /v1/messages/read-all

# 笔记导出
GET     /v1/notes/{noteId}/export/pdf
GET     /v1/notes/{noteId}/export/markdown
```

---

## HTTP 状态码规范

| 操作 | 成功状态码 | 说明 |
|------|-----------|------|
| GET（单个） | 200 | 返回资源 |
| GET（列表） | 200 | 返回资源列表 |
| POST（创建） | 201 | 创建成功 |
| PUT（更新） | 200 | 更新成功，返回更新后数据 |
| DELETE | 200 / 204 | 删除成功（本项目使用 200 + 响应体） |
| POST（操作） | 200 | 操作成功 |

---

## 统一响应格式

所有接口均使用 `ApiResponse<T>` 包装响应：

### 成功响应

```java
// 返回单个对象
return ApiResponse.success(noteVO);

// 返回带消息的单个对象
return ApiResponse.success("Note created successfully", noteVO);

// 返回列表
return ApiResponse.success(noteList);
```

```json
// 单个资源
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "Spring Boot 笔记",
    "content": "...",
    "createTime": "2024-01-15T10:30:00"
  }
}

// 分页列表
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

### 错误响应

```json
{
  "code": 404,
  "message": "Resource not found: Note with id 123",
  "data": null
}
```

**禁止直接抛出未包装的错误响应，必须通过 `GlobalExceptionHandler` 统一处理。**

---

## 分页查询规范

### 请求参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `page` | int | 1 | 当前页码（从 1 开始） |
| `pageSize` | int | 10 | 每页条数 |
| `keyword` | string | — | 关键词搜索（可选） |
| `sortBy` | string | `createTime` | 排序字段（可选） |
| `sortOrder` | string | `desc` | `asc` 或 `desc`（可选） |

```
# 示例
GET /v1/notes?page=2&pageSize=20&keyword=Spring&sortBy=updateTime&sortOrder=desc
```

### Controller 实现

```java
@GetMapping
public ApiResponse<IPage<NoteVO>> listNotes(
        @RequestParam(defaultValue = "1") @Min(1) int page,
        @RequestParam(defaultValue = "10") @Max(100) int pageSize,
        @RequestParam(required = false) String keyword) {
    return ApiResponse.success(noteService.pageNotes(page, pageSize, keyword));
}
```

---

## 请求参数规范

### Path Variable（路径参数）

用于标识特定资源：

```java
// ✅ 正确：使用资源 ID
@GetMapping("/{id}")
public ApiResponse<NoteVO> getNote(@PathVariable Long id) { }

// ✅ 关联资源的 ID 带前缀以区分
@GetMapping("/{courseId}/assignments/{assignmentId}")
public ApiResponse<AssignmentVO> getAssignment(
        @PathVariable Long courseId,
        @PathVariable Long assignmentId) { }
```

### Query Parameter（查询参数）

用于过滤、分页、排序：

```java
// ✅ 过滤条件
@GetMapping
public ApiResponse<IPage<NoteVO>> listNotes(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int pageSize) { }
```

### Request Body（请求体）

用于创建、更新操作：

```java
// ✅ 必须添加 @Valid 注解触发参数校验
@PostMapping
public ApiResponse<NoteVO> createNote(@Valid @RequestBody CreateNoteRequest request) { }

@PutMapping("/{id}")
public ApiResponse<NoteVO> updateNote(
        @PathVariable Long id,
        @Valid @RequestBody UpdateNoteRequest request) { }
```

---

## 时间格式规范

所有时间字段使用 **ISO 8601** 格式（UTC 或带时区偏移）：

```json
{
  "createTime": "2024-01-15T10:30:00",
  "updateTime": "2024-01-15T14:22:33",
  "expiredAt": "2024-02-15T10:30:00+08:00"
}
```

Java 中使用 `LocalDateTime`（存储 UTC 时间），通过 Jackson 配置格式化输出：

```java
// VO 中的时间字段
@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
private LocalDateTime createTime;
```

---

## 接口版本管理

### 当前版本：v1

所有接口路径包含 `/v1/` 前缀：

```java
@RequestMapping("/v1/notes")
```

### 版本升级策略

当 API 需要 **不向后兼容的变更** 时，创建新版本：

```
# v2 API（新版本）
GET /v2/notes

# v1 API（旧版本，维护向后兼容）
GET /v1/notes
```

**需要升级版本的情况：**
- 删除或重命名字段
- 改变字段数据类型
- 改变 URL 结构
- 改变业务语义

**不需要升级版本的情况（向后兼容变更）：**
- 新增可选请求字段
- 新增响应字段
- 新增接口

---

## 安全设计规范

### 认证

```java
// 公开接口（注册、登录、分享链接访问等）明确标注，不需要 Token
// Spring Security 配置中明确 permitAll()
```

### 资源权限校验

```java
// ✅ Service 层校验资源所有权
public NoteVO getNoteById(Long id) {
    NotePO note = noteMapper.selectById(id);
    if (note == null) {
        throw new ResourceNotFoundException("Note", id);
    }
    // 校验当前用户是否有权限访问
    String currentUser = SecurityUtils.getCurrentUser();
    if (!note.getOwnerUsername().equals(currentUser)) {
        throw new BusinessException(403, "Access denied");
    }
    return convertToVO(note);
}

// ❌ 不要只依赖前端传来的 userId，必须从 JWT 获取当前用户
```

### 输入校验

```java
// ✅ 使用 Bean Validation 注解
public class CreateNoteRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最长200字符")
    private String title;

    @Size(max = 1000000, message = "内容超出限制")
    private String content;
}
```

---

## 接口文档规范

所有接口必须添加 Swagger 注解：

```java
@Tag(name = "Note", description = "笔记管理")
@RestController
@RequestMapping("/v1/notes")
public class NoteController {

    @Operation(
        summary = "获取笔记详情",
        description = "根据笔记 ID 获取笔记的完整内容，包括标签信息"
    )
    @ApiResponse(responseCode = "200", description = "成功返回笔记")
    @ApiResponse(responseCode = "404", description = "笔记不存在")
    @ApiResponse(responseCode = "403", description = "无权访问")
    @GetMapping("/{id}")
    public ApiResponse<NoteVO> getNote(
            @Parameter(description = "笔记 ID", required = true)
            @PathVariable Long id) {
        return ApiResponse.success(noteService.getNoteById(id));
    }
}
```

---

## 禁止事项

```
# ❌ 不要在 URL 中使用动词
GET /v1/getNotes
GET /v1/notes/getById/1
POST /v1/notes/create
DELETE /v1/notes/deleteAll

# ❌ 不要使用下划线或驼峰
GET /v1/note_categories
GET /v1/noteCategories

# ❌ 不要在 URL 中暴露实现细节
GET /v1/notes?sql=SELECT * FROM notes

# ❌ 不要忽略 HTTP 状态码
# 所有错误都返回 200，然后用 code 区分（本项目不这样做）
# 本项目 HTTP 状态码与业务 code 保持一致

# ❌ 不要在 GET 请求中使用请求体
# GET 请求参数通过 Query Parameter 传递
```

---

## 相关文档

- [Java 代码规范](./Java代码规范.md)
- [错误码参考](../../技术文档/API文档/错误码参考.md)
- [API 端点概览](../../技术文档/API文档/API端点概览.md)
