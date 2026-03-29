# macOS 开发环境搭建指南

本文档详细说明如何在 macOS 系统上搭建 Course Buddy Backend 本地开发环境。

---

## 环境要求

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| macOS | 12 Monterey+ | 推荐 13 Ventura 或 14 Sonoma |
| JDK | 17（LTS） | 必须 Java 17 |
| Maven | 3.8+ | 或使用项目 mvnw |
| Homebrew | 最新版 | macOS 包管理器 |
| Git | 2.30+ | Xcode Command Line Tools 自带 |
| MySQL | 8.0+ | via Homebrew |
| Redis | 7.0+ | via Homebrew |
| IntelliJ IDEA | 2023.1+ | 推荐 Ultimate 版 |

---

## 第一步：安装 Homebrew

Homebrew 是 macOS 最常用的包管理工具，后续大部分工具都通过它安装。

```bash
# 安装 Homebrew
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Apple Silicon (M1/M2/M3) 用户需额外执行
echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
eval "$(/opt/homebrew/bin/brew shellenv)"

# 验证
brew --version
# Homebrew 4.x.x
```

---

## 第二步：安装 JDK 17

### 方式一：使用 SDKMAN（推荐，支持多版本切换）

```bash
# 安装 SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# 安装 Temurin JDK 17
sdk install java 17.0.13-tem

# 设置为默认
sdk default java 17.0.13-tem

# 验证
java -version
# openjdk version "17.x.x" ...
```

### 方式二：使用 Homebrew

```bash
brew install --cask temurin@17

# 验证
java -version
```

### 配置 JAVA_HOME

```bash
# 添加到 ~/.zshrc（zsh，macOS 默认）或 ~/.bash_profile（bash）
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.zshrc
source ~/.zshrc

# 验证
echo $JAVA_HOME
# /Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
```

---

## 第三步：安装 Git

macOS 通常已自带 Git，但版本较旧。推荐通过 Homebrew 安装最新版：

```bash
brew install git

# 验证
git --version
# git version 2.x.x

# 配置用户信息
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
git config --global core.autocrlf input    # macOS/Linux 推荐设置
```

---

## 第四步：安装 MySQL 8.0

```bash
# 安装 MySQL 8.0
brew install mysql@8.0

# 启动 MySQL 服务
brew services start mysql@8.0

# 添加到 PATH（如果使用 mysql@8.0 非默认版本）
echo 'export PATH="/opt/homebrew/opt/mysql@8.0/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# 配置 root 密码（首次运行）
mysql_secure_installation
# 按提示设置 root 密码，建议选择 STRONG 级别验证

# 验证连接
mysql -u root -p
```

### 创建开发数据库

```sql
-- 连接 MySQL 后执行
CREATE DATABASE IF NOT EXISTS course_buddy
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'course_buddy_user'@'localhost'
    IDENTIFIED BY 'Dev@123456';

GRANT ALL PRIVILEGES ON course_buddy.* TO 'course_buddy_user'@'localhost';
FLUSH PRIVILEGES;

-- 验证
SELECT user, host FROM mysql.user WHERE user = 'course_buddy_user';
SHOW GRANTS FOR 'course_buddy_user'@'localhost';
```

### MySQL 常用命令

```bash
# 启动
brew services start mysql@8.0

# 停止
brew services stop mysql@8.0

# 重启
brew services restart mysql@8.0

# 查看服务状态
brew services list | grep mysql
```

---

## 第五步：安装 Redis 7

```bash
# 安装 Redis
brew install redis

# 启动 Redis 服务（后台运行）
brew services start redis

# 验证
redis-cli ping
# PONG

# 查看版本
redis-cli --version
# redis-cli 7.x.x
```

---

## 第六步：安装 Maven（可选）

项目自带 Maven Wrapper（`./mvnw`），无需单独安装。但如果需要全局使用 Maven：

```bash
brew install maven

# 验证
mvn -version
# Apache Maven 3.x.x
```

---

## 第七步：克隆并配置项目

```bash
# 克隆仓库
git clone <repository-url>
cd course-buddy-backend

# 确认项目结构
ls -la
# 应能看到：pom.xml, Dockerfile, docker-compose.yml, src/, nginx/
```

### 配置开发环境

编辑 `src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/course_buddy?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: course_buddy_user
    password: Dev@123456    # 替换为你的密码
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

## 第八步：配置 IntelliJ IDEA

### 安装 IDEA

```bash
# 通过 Homebrew Cask 安装（Community 版）
brew install --cask intellij-idea-ce

# Ultimate 版（30 天试用或付费）
brew install --cask intellij-idea
```

### 导入项目

1. 启动 IntelliJ IDEA
2. **Open** → 选择 `course-buddy-backend` 根目录（含 `pom.xml`）
3. 等待 Maven 依赖下载完成（首次约 3-5 分钟，视网络情况）

### 配置 SDK

1. **File** → **Project Structure**（`⌘;`）→ **Project**
2. SDK：选择或添加 **JDK 17**
3. Language level：**17 - Sealed types, always-strict floating-point semantics**

### 创建运行配置

1. **Run** → **Edit Configurations**（`⌘,` → 搜索）
2. 点击 **+** → **Spring Boot**
3. 填写：
   - **Name**: `CourseBuddyApp (dev)`
   - **Main class**: `com.coursebuddy.CourseBuddyApplication`
   - **Active profiles**: `dev`
   - **VM options**（可选）: `-Xmx512m -Xms256m`
   - **Environment variables**:
     ```
     JWT_SECRET=dev-local-secret-key-at-least-32-characters-long
     ```

### 推荐 IDEA 插件

```
Lombok                   # 必装
MyBatis-Plus             # SQL 智能提示
.env files support       # .env 语法高亮
Rainbow Brackets         # 彩色括号
GitToolBox               # Git 增强
SonarLint                # 代码质量
Key Promoter X           # 快捷键提示（可选）
```

安装方式：**Settings/Preferences** → **Plugins** → Marketplace 搜索安装

### 启用注解处理

**Preferences** → **Build, Execution, Deployment** → **Compiler** → **Annotation Processors**：
勾选 **Enable annotation processing**

---

## 第九步：运行项目

### 方式一：通过 IDEA 运行（推荐）

点击运行配置旁的 ▶ 按钮，或按 `⌃R`。

### 方式二：通过终端运行

```bash
# 使用 Maven Wrapper（推荐）
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 指定 JWT_SECRET
JWT_SECRET=dev-secret-key-32-chars-minimum ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 验证启动成功

```bash
# 等待出现 "Started CourseBuddyApplication in X.XXX seconds"

# 健康检查
curl http://localhost:8080/api/actuator/health
# {"status":"UP"}
```

访问 Swagger UI：[http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)

---

## Apple Silicon (M1/M2/M3) 注意事项

大部分工具原生支持 Apple Silicon，但有几点需要注意：

```bash
# 确认 Homebrew 安装位置（Apple Silicon 为 /opt/homebrew）
which brew
# /opt/homebrew/bin/brew

# 如果遇到架构问题，确认 Java 版本
java -XshowSettings:all -version 2>&1 | grep "os.arch"
# os.arch = aarch64  (Apple Silicon)
# os.arch = x86_64   (Intel 或 Rosetta)

# 某些 native 依赖（如 Tess4J OCR）在 ARM 上需要额外配置
# 如遇 OCR 功能异常，在 dev 环境可通过配置禁用
```

---

## 常见问题

### 问题 1：Maven 下载慢

配置国内镜像，创建或编辑 `~/.m2/settings.xml`：

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

### 问题 2：MySQL 连接被拒绝

```bash
# 确认 MySQL 正在运行
brew services list | grep mysql
# mysql@8.0  started  ...

# 如果未运行
brew services start mysql@8.0

# 测试连接
mysql -u course_buddy_user -p course_buddy
```

### 问题 3：Redis 连接失败

```bash
# 确认 Redis 运行
brew services list | grep redis
# redis  started  ...

redis-cli ping
# 如果输出 PONG，Redis 正常运行
```

### 问题 4：端口冲突

```bash
# 查找占用 8080 的进程
lsof -i :8080

# 强制停止
kill -9 <PID>
```

### 问题 5：JAVA_HOME 不正确

```bash
# 列出所有已安装的 JDK
/usr/libexec/java_home -V

# 设置正确版本
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
java -version
```

---

## 推荐工具

| 工具 | 说明 | 安装 |
|------|------|------|
| TablePlus | 数据库 GUI（MySQL、Redis 等）| `brew install --cask tableplus` |
| Another Redis Desktop Manager | Redis GUI | GitHub 下载 |
| Postman | API 测试 | `brew install --cask postman` |
| iTerm2 | 增强终端 | `brew install --cask iterm2` |
| Oh My Zsh | Shell 增强 | `sh -c "$(curl -fsSL https://raw.github.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"` |

---

## 下一步

- 阅读 [Java 代码规范](../../代码规范/Java代码规范.md)
- 了解 [API 端点概览](../../../技术文档/API文档/API端点概览.md)
- 配置 [Git 工作流](../../提交规范/Git工作流说明.md)
