# Windows 开发环境搭建指南

本文档详细说明如何在 Windows 系统上搭建 课伴 Backend 本地开发环境。

---

## 环境要求

| 工具 | 版本要求 | 下载地址 |
|------|----------|----------|
| Windows | 10 (1903+) / 11 | — |
| JDK | 17（LTS） | https://adoptium.net/ |
| Maven | 3.8+ | https://maven.apache.org/ |
| Git | 2.30+ | https://git-scm.com/ |
| MySQL | 8.0+ | https://dev.mysql.com/downloads/ |
| Redis | 7.0+ | Windows 版见下文 |
| IDE | IntelliJ IDEA 2023.1+ | https://www.jetbrains.com/idea/ |

---

## 第一步：安装 JDK 17

### 方式一：使用 Adoptium（推荐）

1. 访问 [https://adoptium.net/](https://adoptium.net/)
2. 选择 **Temurin 17 (LTS)** → **Windows x64** → **JDK**
3. 下载 `.msi` 安装包并安装
4. 安装时勾选 **"Set JAVA_HOME variable"** 和 **"Add to PATH"**

### 方式二：使用 winget（Windows 11/Windows 10 新版）

```powershell
winget install EclipseAdoptium.Temurin.17.JDK
```

### 验证安装

```powershell
java -version
# 预期输出：openjdk version "17.x.x" ...

javac -version
# 预期输出：javac 17.x.x
```

### 配置 JAVA_HOME（如未自动配置）

1. 打开 **系统属性** → **高级** → **环境变量**
2. 新建系统变量：
   - 变量名：`JAVA_HOME`
   - 变量值：`C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot`
3. 在 `Path` 变量中添加：`%JAVA_HOME%\bin`

---

## 第二步：安装 Git

```powershell
winget install Git.Git
```

或从 [https://git-scm.com/download/win](https://git-scm.com/download/win) 下载安装。

**推荐配置：**

```bash
# 打开 Git Bash 执行
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
git config --global core.autocrlf true    # Windows 换行符自动转换
git config --global core.editor "code --wait"  # 使用 VS Code 作为编辑器（可选）
```

---

## 第三步：安装 MySQL 8.0

### 方式一：MySQL Installer

1. 下载 [MySQL Installer](https://dev.mysql.com/downloads/installer/)
2. 选择 **Custom** 安装类型
3. 勾选 **MySQL Server 8.0.x**
4. 设置 root 密码（记住，后续配置需要用到）
5. 配置为 Windows 服务（默认启动）

### 方式二：winget

```powershell
winget install Oracle.MySQL
```

### 创建开发数据库

打开 MySQL Command Line Client 或使用 MySQL Workbench：

```sql
CREATE DATABASE IF NOT EXISTS course_buddy
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'course_buddy_user'@'localhost'
    IDENTIFIED BY 'Dev@123456';

GRANT ALL PRIVILEGES ON course_buddy.* TO 'course_buddy_user'@'localhost';
FLUSH PRIVILEGES;

-- 验证
SHOW DATABASES LIKE 'course_buddy';
```

---

## 第四步：安装 Redis（Windows）

Redis 官方不提供 Windows 版本，推荐以下方式之一：

### 方式一：使用 Memurai（Redis 兼容，推荐用于开发）

1. 下载 [Memurai](https://www.memurai.com/) Developer 版（免费）
2. 安装并启动服务

### 方式二：使用 Docker Desktop 中的 Redis

如果已安装 Docker Desktop：

```powershell
docker run -d --name redis-dev -p 6379:6379 redis:7.2-alpine
```

### 方式三：WSL2 + Redis

```bash
# 在 WSL2 中执行
sudo apt-get install redis-server
sudo service redis-server start
redis-cli ping  # PONG
```

### 验证 Redis

```powershell
# 使用 redis-cli（需在安装目录或 WSL 中）
redis-cli -h 127.0.0.1 -p 6379 ping
# PONG
```

---

## 第五步：克隆项目

```powershell
# 打开 PowerShell 或 Git Bash
git clone <repository-url>
cd course-buddy-backend
```

---

## 第六步：配置开发环境

编辑 `src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/course_buddy?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: course_buddy_user   # 或 root
    password: Dev@123456          # 替换为你的密码
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 5
  data:
    redis:
      host: localhost
      port: 6379
  flyway:
    enabled: true

logging:
  level:
    root: INFO
    com.coursebuddy: DEBUG
    org.springframework.security: DEBUG
```

---

## 第七步：配置 IntelliJ IDEA

### 导入项目

1. 启动 IntelliJ IDEA
2. **File** → **Open** → 选择 `course-buddy-backend` 目录（包含 `pom.xml` 的目录）
3. 选择 **Open as Project**
4. 等待 Maven 下载依赖（首次需要几分钟）

### 配置 JDK

1. **File** → **Project Structure** → **Project**
2. SDK 选择 **17**（如未显示，点击 **Edit** → **+** → **Add JDK** → 选择 JDK 17 安装目录）
3. Language level 选择 **17**

### 配置运行配置

1. 打开 **Run** → **Edit Configurations**
2. 点击 **+** → **Spring Boot**
3. 配置如下：
   - **Name**: `CourseBuddyApp (dev)`
   - **Main class**: `com.coursebuddy.CourseBuddyApplication`
   - **Active profiles**: `dev`
   - **Environment variables**（可选，如需覆盖配置文件中的值）：
     ```
     JWT_SECRET=dev-local-secret-key-at-least-32-characters-long
     ```

### 推荐安装的 IDEA 插件

| 插件 | 说明 |
|------|------|
| Lombok | 必装，解析 `@Data`、`@Builder` 等注解 |
| MyBatis-Plus | MyBatis-Plus 代码提示 |
| .env files support | `.env` 文件语法高亮 |
| GitToolBox | Git 增强工具 |
| SonarLint | 代码质量检查 |

**安装 Lombok 支持：**

1. **File** → **Settings** → **Build, Execution, Deployment** → **Compiler** → **Annotation Processors**
2. 勾选 **Enable annotation processing**

---

## 第八步：运行项目

### 方式一：通过 IDEA 运行

点击 `CourseBuddyApp (dev)` 旁边的绿色三角 ▶ 运行。

观察控制台输出，当看到类似以下日志时表示启动成功：

```
Started CourseBuddyApplication in 8.xxx seconds (process running for 9.xxx)
```

### 方式二：通过命令行运行

```powershell
# 在项目根目录
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### 验证启动

```powershell
# PowerShell
Invoke-WebRequest -Uri "http://localhost:8080/api/actuator/health" | Select-Object Content
# 预期：{"status":"UP"}

# 或使用 curl（Windows 10+ 内置）
curl http://localhost:8080/api/actuator/health
```

访问 Swagger UI：[http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)

---

## 常见问题

### 问题 1：Maven 依赖下载缓慢

配置国内镜像，编辑或创建 `%USERPROFILE%\.m2\settings.xml`：

```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven Mirror</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
```

### 问题 2：端口 3306 被占用

```powershell
# 查找占用 3306 端口的进程
netstat -ano | findstr :3306

# 根据 PID 停止进程
taskkill /PID <pid> /F
```

### 问题 3：Flyway 迁移失败

```
Error: Migration checksum mismatch
```

**解决方案**：清空 `flyway_schema_history` 表（仅开发环境）：

```sql
DROP TABLE IF EXISTS flyway_schema_history;
```

然后重新启动应用，Flyway 会重新执行所有迁移。

### 问题 4：Redis 无法连接

确认 Redis 服务正在运行：

```powershell
# Memurai 用户
Get-Service Memurai

# Docker 用户
docker ps | findstr redis
```

### 问题 5：JAVA_HOME 未设置

```powershell
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot", "Machine")
```

---

## 推荐工具

| 工具 | 用途 | 下载 |
|------|------|------|
| TablePlus / DBeaver | 数据库 GUI 客户端 | 各官网 |
| Another Redis Desktop Manager | Redis GUI 客户端 | GitHub |
| Postman / Insomnia | API 测试 | 各官网 |
| Windows Terminal | 更好的终端体验 | Microsoft Store |

---

## 下一步

- 阅读 [Java 代码规范](../../代码规范/Java代码规范.md)
- 了解 [API 端点概览](../../../技术文档/API文档/API端点概览.md)
- 配置 [Git 工作流](../../提交规范/Git工作流说明.md)
