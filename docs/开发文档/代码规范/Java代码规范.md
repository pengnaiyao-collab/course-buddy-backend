# Java 代码规范

本文档定义 Course Buddy Backend 项目的 Java 编码标准，所有开发者提交代码前必须遵守。

---

## 基本原则

1. **可读性优先**：代码首先是写给人看的，其次才是给机器执行的
2. **一致性**：整个项目保持统一的代码风格
3. **简洁性**：不写无用代码，不过度设计
4. **单一职责**：每个类、每个方法只做一件事

---

## 命名规范

### 包命名

采用全小写，使用点分隔，遵循项目基础包名 `com.coursebuddy`：

```java
com.coursebuddy.controller          // Controller 层
com.coursebuddy.service             // Service 接口
com.coursebuddy.service.impl        // Service 实现
com.coursebuddy.mapper              // MyBatis-Plus Mapper
com.coursebuddy.domain.po           // 持久化对象（数据库实体）
com.coursebuddy.domain.dto          // 数据传输对象（请求参数）
com.coursebuddy.domain.vo           // 视图对象（响应数据）
com.coursebuddy.config              // 配置类
com.coursebuddy.common              // 公共工具类
com.coursebuddy.common.exception    // 异常类
com.coursebuddy.aop                 // AOP 切面
```

### 类命名

使用 **UpperCamelCase（大驼峰）**：

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| Controller | `{功能}Controller` | `NoteController`, `AuthController` |
| Service 接口 | `I{功能}Service` | `INoteService`, `ICourseService` |
| Service 实现 | `{功能}ServiceImpl` | `NoteServiceImpl` |
| Mapper | `{功能}Mapper` | `NoteMapper`, `UserMapper` |
| 持久化对象 PO | `{功能}PO` | `NotePO`, `CoursePO` |
| 数据传输对象 DTO | `{功能}DTO` 或 `{操作}Request` | `NoteDTO`, `CreateNoteRequest` |
| 视图对象 VO | `{功能}VO` | `NoteVO`, `CourseVO` |
| 配置类 | `{功能}Config` | `SecurityConfig`, `MinIOConfig` |
| 工具类 | `{功能}Utils` | `SecurityUtils`, `JwtUtils` |
| 异常类 | `{描述}Exception` | `BusinessException`, `ResourceNotFoundException` |

### 方法命名

使用 **lowerCamelCase（小驼峰）**，动词开头：

```java
// ✅ 好的命名
public NoteVO getNoteById(Long id)
public List<NoteVO> listNotesByUser(String username)
public NoteVO createNote(CreateNoteRequest request)
public NoteVO updateNote(Long id, UpdateNoteRequest request)
public void deleteNote(Long id)
public boolean isNoteOwner(Long noteId, String username)
public void sendNotification(Long userId, String message)

// ❌ 不好的命名
public NoteVO note(Long id)        // 含义不明确
public NoteVO getNote(Long id)     // 可以，但不如 getNoteById 清晰
public void doDelete(Long id)      // do 前缀无意义
public NoteVO fetchNoteData(Long id) // fetch/Data 冗余
```

| 前缀 | 含义 | 示例 |
|------|------|------|
| `get` | 获取单个 | `getNoteById`, `getCurrentUser` |
| `list` | 获取列表 | `listNotesByUser`, `listCourses` |
| `page` | 分页查询 | `pageNotes`, `pageCourses` |
| `create` | 创建 | `createNote`, `createCourse` |
| `update` | 更新 | `updateNote`, `updateProfile` |
| `delete` | 删除 | `deleteNote`, `deleteCourse` |
| `save` | 保存（创建或更新） | `saveNote` |
| `is`/`has`/`can` | 布尔判断 | `isOwner`, `hasPermission`, `canEdit` |
| `send` | 发送 | `sendEmail`, `sendNotification` |
| `export` | 导出 | `exportToPdf`, `exportToMarkdown` |

### 变量命名

```java
// ✅ 好的命名
String username;
Long courseId;
List<NoteVO> noteList;
Map<String, Object> resultMap;
boolean isActive;
int maxRetryCount;

// ❌ 避免的命名
String n;           // 无意义缩写
String uname;       // 缩写不统一
List list1;         // 数字后缀
Object temp;        // temp/tmp/obj 等无意义名称
int a, b, c;        // 除非是数学公式中的变量
```

### 常量命名

使用全大写下划线分隔：

```java
public static final int MAX_RETRY_COUNT = 3;
public static final String DEFAULT_CHARSET = "UTF-8";
public static final long JWT_EXPIRATION_MS = 86400000L;
```

---

## 分层架构规范

项目采用三层架构：`Controller → Service → Mapper/Repository`

### Controller 层规范

```java
@Tag(name = "Note", description = "笔记管理接口")
@RestController
@RequestMapping("/v1/notes")
@RequiredArgsConstructor    // Lombok 构造器注入（替代 @Autowired）
public class NoteController {

    private final INoteService noteService;  // 注入 Service 接口，不注入实现类

    @Operation(summary = "获取笔记列表")
    @GetMapping
    public ApiResponse<IPage<NoteVO>> listNotes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(noteService.pageNotes(page, pageSize));
    }

    @Operation(summary = "创建新笔记")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NoteVO> createNote(@Valid @RequestBody CreateNoteRequest request) {
        return ApiResponse.success("Note created successfully", noteService.createNote(request));
    }
}
```

**Controller 层职责：**
- ✅ 接收并校验 HTTP 请求参数（使用 `@Valid`）
- ✅ 调用 Service 层业务方法
- ✅ 封装统一响应体 `ApiResponse<T>`
- ❌ **不处理业务逻辑**（业务判断、数据库查询等）
- ❌ **不直接操作 Mapper/Repository**

### Service 层规范

```java
// 接口定义
public interface INoteService {
    IPage<NoteVO> pageNotes(int page, int pageSize);
    NoteVO getNoteById(Long id);
    NoteVO createNote(CreateNoteRequest request);
    NoteVO updateNote(Long id, UpdateNoteRequest request);
    void deleteNote(Long id);
}

// 实现类
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements INoteService {

    private final NoteMapper noteMapper;
    private final IMinIOUploadService minIOUploadService;  // 可以依赖其他 Service

    @Override
    public NoteVO getNoteById(Long id) {
        NotePO note = noteMapper.selectById(id);
        if (note == null) {
            throw new ResourceNotFoundException("Note", id);
        }

        String currentUser = SecurityUtils.getCurrentUser();
        if (!note.getOwnerUsername().equals(currentUser)) {
            throw new BusinessException(403, "Access denied");
        }

        return convertToVO(note);
    }

    // 私有方法：PO → VO 转换
    private NoteVO convertToVO(NotePO po) {
        NoteVO vo = new NoteVO();
        vo.setId(po.getId());
        vo.setTitle(po.getTitle());
        // ...
        return vo;
    }
}
```

**Service 层职责：**
- ✅ 业务逻辑处理
- ✅ 事务控制（`@Transactional`）
- ✅ 调用 Mapper 进行数据操作
- ✅ 调用其他 Service
- ✅ 抛出业务异常
- ❌ 不处理 HTTP 请求/响应细节

### Mapper 层规范

```java
@Mapper
public interface NoteMapper extends BaseMapper<NotePO> {
    // 简单 CRUD 使用 MyBatis-Plus 提供的方法，无需额外定义

    // 复杂查询定义自定义方法
    @Select("SELECT * FROM notes WHERE owner_username = #{username} AND deleted_at IS NULL ORDER BY create_time DESC")
    List<NotePO> selectByOwner(@Param("username") String username);

    // 复杂 SQL 建议使用 XML Mapper 文件
    List<NoteVO> selectNotesWithTags(@Param("userId") Long userId);
}
```

---

## 实体对象规范

### PO（持久化对象）

```java
@Data                           // Lombok：生成 getter/setter/toString/equals/hashCode
@TableName("notes")             // MyBatis-Plus：映射表名
public class NotePO {

    @TableId(type = IdType.AUTO) // 自增主键
    private Long id;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("owner_username")
    private String ownerUsername;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    // 逻辑删除字段（MyBatis-Plus 自动处理）
    @TableLogic(value = "NULL", delval = "NOW()")
    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
```

### DTO（请求参数对象）

```java
@Data
public class CreateNoteRequest {

    @NotBlank(message = "笔记标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    @Schema(description = "笔记标题", example = "Spring Boot 学习笔记")
    private String title;

    @Schema(description = "笔记内容（Markdown 格式）")
    private String content;

    @Schema(description = "分类 ID")
    private Long categoryId;
}
```

### VO（响应视图对象）

```java
@Data
public class NoteVO {
    private Long id;
    private String title;
    private String content;
    private String ownerUsername;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<String> tags;  // 可以包含关联数据
}
```

---

## 异常处理规范

### 使用项目提供的异常类

```java
// 资源不存在时
throw new ResourceNotFoundException("Note", noteId);
// → HTTP 404: {"code": 404, "message": "Resource not found: Note with id 123"}

// 业务规则违反时（默认 400）
throw new BusinessException("Username already exists");
// → HTTP 400: {"code": 400, "message": "Username already exists"}

// 自定义 code 的业务异常
throw new BusinessException(403, "You don't have permission");
// → HTTP 400（@ResponseStatus 注解决定 HTTP 状态码），业务 code=403
```

### 不要滥用 try-catch

```java
// ❌ 错误示例：吞掉异常
try {
    noteMapper.insert(notePO);
} catch (Exception e) {
    // 不要这样做
}

// ✅ 正确示例：让异常向上传播，由 GlobalExceptionHandler 统一处理
noteMapper.insert(notePO);

// ✅ 正确示例：捕获后处理，然后重新抛出
try {
    minioClient.putObject(args);
} catch (Exception e) {
    log.error("Failed to upload file to MinIO: {}", e.getMessage(), e);
    throw new BusinessException("File upload failed");
}
```

---

## 注解使用规范

### Lombok 注解

```java
@Data               // 等同于 @Getter + @Setter + @ToString + @EqualsAndHashCode + @RequiredArgsConstructor
@Builder            // 建造者模式
@NoArgsConstructor  // 无参构造（与 @Builder 配合使用时需要）
@AllArgsConstructor // 全参构造
@RequiredArgsConstructor  // final 字段构造（用于依赖注入，替代 @Autowired）
@Slf4j              // 注入 log 变量（等同于 private static final Logger log = ...）
```

**Controller/Service 推荐组合：**
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements INoteService {
    private final NoteMapper noteMapper;  // 通过构造器注入，无需 @Autowired
    // ...
}
```

### 事务注解

```java
// Service 实现类中需要事务的方法
@Transactional(rollbackFor = Exception.class)
public NoteVO createNote(CreateNoteRequest request) {
    // 多步数据库操作，任意一步失败则整体回滚
    NotePO note = new NotePO();
    noteMapper.insert(note);

    NoteTagPO tag = new NoteTagPO();
    tagMapper.insert(tag);

    return convertToVO(note);
}

// 只读事务（提升性能）
@Transactional(readOnly = true)
public NoteVO getNoteById(Long id) {
    return convertToVO(noteMapper.selectById(id));
}
```

---

## 日志规范

```java
@Slf4j
@Service
public class NoteServiceImpl {

    public NoteVO createNote(CreateNoteRequest request) {
        // ✅ 使用参数化日志，避免字符串拼接（性能优化）
        log.debug("Creating note for user: {}", SecurityUtils.getCurrentUser());

        // ✅ 业务关键操作记录 INFO 日志
        log.info("Note created successfully: noteId={}, user={}", note.getId(), username);

        // ✅ 可预期的异常用 WARN
        log.warn("Note not found: id={}", id);

        // ✅ 未预期的异常用 ERROR，并附带 cause
        log.error("Failed to export note to PDF: noteId={}", id, e);

        // ❌ 不要在日志中打印密码、Token 等敏感信息
        log.info("User login: username={}", username);  // ✅
        log.info("User login: username={}, password={}", username, password);  // ❌
    }
}
```

---

## 代码格式规范

### 缩进与空格

- 缩进：**4 个空格**（不使用 Tab）
- 行宽：最大 **120 个字符**
- 方法之间：空一行
- 逻辑块之间：适当空行增加可读性

### 导入语句

- 不使用通配符导入（`import com.coursebuddy.*`）
- 按 IDEA 默认分组顺序排列：标准库 → 第三方库 → 项目内部

```java
// ✅ 正确
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.domain.po.NotePO;

// ❌ 错误
import com.coursebuddy.*;
import org.springframework.*;
```

---

## MyBatis-Plus 使用规范

```java
// ✅ 使用 LambdaQueryWrapper 避免硬编码字段名
LambdaQueryWrapper<NotePO> wrapper = Wrappers.<NotePO>lambdaQuery()
    .eq(NotePO::getOwnerUsername, username)
    .eq(NotePO::getCategoryId, categoryId)
    .orderByDesc(NotePO::getCreateTime);

List<NotePO> notes = noteMapper.selectList(wrapper);

// ✅ 分页查询
Page<NotePO> page = new Page<>(pageNum, pageSize);
IPage<NotePO> result = noteMapper.selectPage(page, wrapper);

// ✅ 更新部分字段
LambdaUpdateWrapper<NotePO> updateWrapper = Wrappers.<NotePO>lambdaUpdate()
    .eq(NotePO::getId, id)
    .set(NotePO::getTitle, newTitle)
    .set(NotePO::getUpdateTime, LocalDateTime.now());
noteMapper.update(null, updateWrapper);

// ❌ 避免在 Service 中写原生 SQL（使用 Mapper XML 或 @Select）
// ❌ 避免 selectAll 后在内存中过滤（数据量大时性能差）
```

---

## 相关资源

- [阿里巴巴 Java 开发手册（泰山版）](https://github.com/alibaba/p3c)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [REST API 设计规范](./REST_API设计规范.md)
