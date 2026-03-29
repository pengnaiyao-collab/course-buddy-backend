# Swagger / OpenAPI 配置说明

本文档说明 Course Buddy Backend 的 Swagger UI 和 OpenAPI 文档的访问方式、配置说明及使用技巧。

---

## 概述

项目使用 **springdoc-openapi 2.3.0** 自动生成 OpenAPI 3.0 规范文档，并提供 Swagger UI 交互式界面，方便开发者浏览和调试 API。

| 项目 | 技术 | 版本 |
|------|------|------|
| API 规范 | OpenAPI | 3.0 |
| UI 框架 | Swagger UI | 内嵌于 springdoc-openapi |
| 依赖 | springdoc-openapi-starter-webmvc-ui | 2.3.0 |

---

## 访问地址

| 端点 | URL | 说明 |
|------|-----|------|
| Swagger UI | `http://localhost:8080/api/swagger-ui.html` | 交互式 API 文档界面 |
| OpenAPI JSON | `http://localhost:8080/api/v3/api-docs` | 机器可读的 API 规范 |
| OpenAPI YAML | `http://localhost:8080/api/v3/api-docs.yaml` | YAML 格式规范 |

> **注意**：所有地址均包含 `/api` context-path 前缀（在 `application.yml` 中配置：`server.servlet.context-path: /api`）

---

## 配置说明

### `application.yml` 中的 Swagger 配置

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs          # OpenAPI JSON 路径
  swagger-ui:
    path: /swagger-ui.html      # Swagger UI 路径
    operations-sorter: method   # 按 HTTP 方法排序（GET/POST/PUT/DELETE）
```

### Spring Security 放行配置

Swagger 相关路径已在 Spring Security 中配置为公开访问，无需认证：

```java
// SecurityConfig.java（示例）
http.authorizeHttpRequests(auth -> auth
    .requestMatchers(
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    ).permitAll()
    // ... 其他配置
);
```

---

## 代码中的 Swagger 注解

### Controller 层注解

```java
// 为 Controller 添加标签说明
@Tag(name = "Authentication", description = "Login and registration endpoints")
@RestController
@RequestMapping("/auth")
public class AuthController {

    // 为接口方法添加说明
    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // ...
    }

    @Operation(summary = "Login with username and password")
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // ...
    }
}
```

### 常用注解说明

| 注解 | 位置 | 说明 |
|------|------|------|
| `@Tag` | 类 | 为 Controller 分组命名 |
| `@Operation` | 方法 | 描述 API 操作 |
| `@Parameter` | 参数 | 描述请求参数 |
| `@RequestBody`（springdoc）| 方法 | 描述请求体 |
| `@ApiResponse` | 方法 | 描述响应 |
| `@Schema` | DTO 类/字段 | 描述数据模型 |

### DTO 注解示例

```java
@Schema(description = "用户登录请求")
public class LoginRequest {

    @Schema(description = "用户名", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", example = "Test@123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;
}
```

---

## 在 Swagger UI 中测试需要认证的接口

Course Buddy Backend 使用 JWT Bearer Token 认证。在 Swagger UI 中测试受保护的接口，需要先获取 Token 并配置。

### 步骤

**第 1 步：登录获取 Token**

1. 在 Swagger UI 中找到 `POST /v1/auth/login`
2. 点击 **Try it out**
3. 填写请求体：
   ```json
   {
     "username": "testuser",
     "password": "Test@123456"
   }
   ```
4. 点击 **Execute**
5. 从响应中复制 `token` 字段的值

**第 2 步：配置 Bearer Token**

1. 点击页面右上角的 **Authorize** 按钮（🔒 图标）
2. 在弹出框的 `bearerAuth` 部分
3. 在 **Value** 字段填入 Token（注意：**不需要**加 `Bearer ` 前缀，Swagger UI 会自动添加）
4. 点击 **Authorize** → **Close**

**第 3 步：测试受保护接口**

完成上述配置后，所有标有 🔒 图标的接口都会自动携带 Bearer Token。

---

## 全局 OpenAPI 配置（自定义）

如需自定义全局信息（标题、描述、版本、安全方案等），可以在配置类中添加 `OpenAPI` Bean：

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI courseBuddyOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Course Buddy Backend API")
                .description("课程伙伴平台后端接口文档")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Course Buddy Team")
                    .email("dev@coursebuddy.example.com")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("在此输入 JWT Token（不需要 Bearer 前缀）")));
    }
}
```

---

## 生产环境建议

在生产环境中，通常建议**关闭** Swagger UI 或限制访问，以防止 API 信息泄露：

### 方式一：生产环境禁用

在 `application-prod.yml` 中添加：

```yaml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

### 方式二：限制访问 IP

在 Nginx 配置中限制 Swagger 路径的访问：

```nginx
location /api/swagger-ui {
    allow 10.0.0.0/8;    # 仅内网访问
    deny all;
    proxy_pass http://app:8080;
}

location /api/v3/api-docs {
    allow 10.0.0.0/8;
    deny all;
    proxy_pass http://app:8080;
}
```

### 方式三：添加认证保护

在 Spring Security 中要求 Swagger 路径也需要认证（仅允许管理员访问）。

---

## 导出 API 文档

### 导出为 JSON

```bash
# 本地运行时
curl -o openapi.json http://localhost:8080/api/v3/api-docs

# 格式化输出
curl -s http://localhost:8080/api/v3/api-docs | python3 -m json.tool > openapi-formatted.json
```

### 导出为 YAML

```bash
curl -o openapi.yaml http://localhost:8080/api/v3/api-docs.yaml
```

### 生成客户端代码（可选）

使用 OpenAPI Generator 根据 API 规范生成客户端 SDK：

```bash
# 安装 OpenAPI Generator CLI
npm install -g @openapitools/openapi-generator-cli

# 生成 TypeScript/Axios 客户端
openapi-generator-cli generate \
  -i http://localhost:8080/api/v3/api-docs \
  -g typescript-axios \
  -o ./generated/typescript-client

# 生成 Python 客户端
openapi-generator-cli generate \
  -i http://localhost:8080/api/v3/api-docs \
  -g python \
  -o ./generated/python-client
```

---

## 常见问题

### 问题 1：Swagger UI 无法访问

1. 确认应用已启动：`curl http://localhost:8080/api/actuator/health`
2. 确认路径正确：注意 context-path `/api` 前缀
3. 检查 Spring Security 配置是否放行了 Swagger 路径

### 问题 2：Token 过期后接口返回 401

- JWT Token 默认有效期为 **24 小时**（86400000 毫秒，配置于 `jwt.expiration`）
- 过期后需重新登录获取新 Token，再通过 **Authorize** 按钮重新配置

### 问题 3：接口没有出现在 Swagger UI 中

常见原因：
- Controller 类没有 `@RestController` 注解
- 包路径不在 `com.coursebuddy` 扫描范围内
- 方法没有 `@RequestMapping` 相关注解

### 问题 4：请求体 Schema 显示不正确

确保 DTO 类使用了正确注解：
- `@Schema` 描述字段
- Jakarta Validation 注解（`@NotBlank`、`@NotNull` 等）会自动转换为 required 约束

---

## 相关资源

- [springdoc-openapi 官方文档](https://springdoc.org/)
- [OpenAPI 3.0 规范](https://swagger.io/specification/)
- [Swagger UI 使用指南](https://swagger.io/tools/swagger-ui/)
- [API 端点概览](./API端点概览.md)
- [认证接口文档](./认证接口文档.md)
