# Docker 本地开发环境指南

Docker 方式是最简单、最一致的本地开发环境搭建方法，无需在本地安装 MySQL、Redis 等依赖服务。

---

## 适用场景

| 场景 | 推荐方案 |
|------|----------|
| 快速体验项目 | Docker 全栈启动（本文） |
| 日常开发调试 | Docker 仅启动依赖 + 本地运行 Spring Boot（推荐） |
| CI/CD 环境 | Docker 全栈（含构建） |
| 生产部署参考 | 参见 [Docker 部署指南](../../../运维文档/部署指南/Docker部署指南.md) |

---

## 前置要求

### 安装 Docker Desktop

**Windows / macOS：**

1. 下载 [Docker Desktop](https://www.docker.com/products/docker-desktop/)
2. 安装并启动 Docker Desktop
3. （macOS Apple Silicon）Docker Desktop 4.x+ 原生支持 ARM64

**Linux：**

```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER  # 免 sudo 使用 docker
newgrp docker                   # 立即生效

# 验证
docker --version
docker compose version
```

### 最低配置要求

| 资源 | 最低 | 推荐 |
|------|------|------|
| CPU | 2 核 | 4 核 |
| 内存 | 4 GB 分配给 Docker | 8 GB |
| 磁盘 | 5 GB 可用空间 | 10 GB |

---

## 方案一：完整 Docker 环境（快速启动）

所有服务（MySQL、Redis、应用、Nginx）均运行在容器中。

### 步骤

```bash
# 1. 克隆仓库
git clone <repository-url>
cd course-buddy-backend

# 2. 创建 .env 文件
cat > .env << 'EOF'
DB_NAME=course_buddy
DB_USER=course_buddy_user
DB_PASSWORD=changeme_dev
MYSQL_ROOT_PASSWORD=rootpassword_dev
JWT_SECRET=dev-only-secret-key-please-change-in-production-32chars
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
XUNFEI_APP_ID=
XUNFEI_API_KEY=
XUNFEI_API_SECRET=
EOF

# 3. 构建并启动所有服务
docker compose up -d --build

# 4. 查看启动状态
docker compose ps

# 5. 查看应用日志
docker compose logs -f app
```

### 等待启动完成

Spring Boot 应用启动约需 60-90 秒（首次构建更长），等待如下健康检查变为 `(healthy)`：

```bash
docker compose ps
# NAME                    COMMAND                  SERVICE   STATUS
# course-buddy-app        "java -jar app.jar"      app       running (healthy)
# course-buddy-mysql      "docker-entrypoint.s…"   mysql     running (healthy)
# course-buddy-nginx      "/docker-entrypoint.…"   nginx     running (healthy)
# course-buddy-redis      "docker-entrypoint.s…"   redis     running (healthy)
```

### 验证

```bash
# 直接访问 Spring Boot
curl http://localhost:8080/api/actuator/health
# {"status":"UP"}

# 通过 Nginx 访问
curl http://localhost/api/actuator/health
# {"status":"UP"}

# 打开 Swagger UI
open http://localhost:8080/api/swagger-ui.html   # macOS
xdg-open http://localhost:8080/api/swagger-ui.html  # Linux
# Windows: 浏览器直接访问 http://localhost:8080/api/swagger-ui.html
```

---

## 方案二：仅容器化依赖（推荐日常开发）

MySQL 和 Redis 运行在容器中，Spring Boot 应用在本地运行（支持热重载、调试）。

### 启动依赖服务

```bash
# 仅启动 MySQL 和 Redis
docker compose up -d mysql redis

# 等待健康检查通过
docker compose ps mysql redis
# mysql   running (healthy)
# redis   running (healthy)
```

### 配置本地连接

开发配置 `src/main/resources/application-dev.yml` 默认连接 localhost，无需修改即可连接容器中的 MySQL 和 Redis（因为容器端口已映射到宿主机）。

```yaml
# application-dev.yml 默认配置（无需修改）
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/course_buddy?...
  data:
    redis:
      host: localhost
      port: 6379
```

### 本地运行 Spring Boot

```bash
# 确保 JDK 17 已安装
java -version

# 运行
JWT_SECRET=dev-secret-32-chars-minimum ./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=dev
```

### 优势

- ✅ 热重载（`spring-boot-devtools` 或 IDEA 自动编译）
- ✅ 可以断点调试
- ✅ 代码修改立即生效
- ✅ 无需每次修改都重新构建镜像

---

## 方案三：开发容器（Dev Container）

如果使用 VS Code 和 Dev Containers 扩展，可以在容器内完成所有开发工作。

### 配置文件

在项目根目录创建 `.devcontainer/devcontainer.json`（如果还不存在）：

```json
{
  "name": "Course Buddy Backend Dev",
  "dockerComposeFile": "../docker-compose.yml",
  "service": "app",
  "workspaceFolder": "/workspace",
  "customizations": {
    "vscode": {
      "extensions": [
        "vscjava.vscode-java-pack",
        "redhat.vscode-xml",
        "ms-azuretools.vscode-docker"
      ]
    }
  }
}
```

---

## docker-compose.yml 结构说明

```yaml
services:
  mysql:
    image: mysql:8.0
    # 端口映射：宿主机 3306 → 容器 3306
    # 数据持久化：mysql_data 卷
    # 字符集：utf8mb4

  redis:
    image: redis:7.2-alpine
    # 端口映射：宿主机 6379 → 容器 6379
    # 数据持久化：redis_data 卷

  app:
    build: .  # 使用项目根目录的 Dockerfile 构建
    depends_on:
      mysql:
        condition: service_healthy  # 等待 MySQL 健康检查通过
      redis:
        condition: service_healthy
    # 健康检查：GET /api/actuator/health

  nginx:
    image: nginx:1.25-alpine
    # 反向代理：80/443 → app:8080
    # 配置文件：./nginx/nginx.conf
```

---

## 常用 Docker 命令

### 服务管理

```bash
# 启动所有服务（后台）
docker compose up -d

# 启动并实时查看日志
docker compose up

# 停止所有服务（保留数据卷）
docker compose down

# 停止并删除数据卷（彻底清除所有数据）
docker compose down -v

# 重启某个服务
docker compose restart app

# 查看服务状态
docker compose ps
```

### 日志查看

```bash
# 查看所有服务日志
docker compose logs

# 实时跟踪应用日志
docker compose logs -f app

# 查看最近 100 行
docker compose logs --tail=100 app

# 查看 MySQL 日志
docker compose logs -f mysql

# 查看 Redis 日志
docker compose logs -f redis
```

### 镜像构建

```bash
# 重新构建应用镜像（代码变更后）
docker compose build app

# 重新构建并启动
docker compose up -d --build app

# 强制不使用缓存重新构建
docker compose build --no-cache app
```

### 进入容器

```bash
# 进入应用容器
docker compose exec app sh

# 进入 MySQL 容器
docker compose exec mysql mysql -u course_buddy_user -p course_buddy

# 进入 Redis 容器
docker compose exec redis redis-cli
```

### 数据管理

```bash
# 查看所有数据卷
docker volume ls | grep course-buddy

# 备份 MySQL 数据
docker compose exec mysql mysqldump \
  -u root -prootpassword_dev course_buddy > backup.sql

# 恢复 MySQL 数据
docker compose exec -T mysql mysql \
  -u root -prootpassword_dev course_buddy < backup.sql
```

---

## 开发调试技巧

### 连接容器内的 MySQL

```bash
# 从宿主机连接（需要本地 mysql-client）
mysql -h 127.0.0.1 -P 3306 -u course_buddy_user -p course_buddy

# 使用 Docker exec
docker compose exec mysql mysql -u course_buddy_user -p course_buddy
```

### 连接容器内的 Redis

```bash
# 从宿主机连接（需要本地 redis-cli）
redis-cli -h 127.0.0.1 -p 6379

# 使用 Docker exec
docker compose exec redis redis-cli

# 常用 Redis 命令
redis-cli -h 127.0.0.1 keys "*"        # 查看所有 key
redis-cli -h 127.0.0.1 flushdb          # 清空数据（开发环境）
```

### 远程调试 Spring Boot

如需调试容器内运行的 Spring Boot，修改 `docker-compose.yml` 中 app 服务：

```yaml
app:
  environment:
    JAVA_OPTS: "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
  ports:
    - "8080:8080"
    - "5005:5005"   # 调试端口
```

然后在 IDEA 中配置 **Remote JVM Debug**，端口 5005。

---

## 环境变量参考

`.env` 文件中可配置的所有变量：

```bash
# ============ 数据库 ============
DB_NAME=course_buddy           # 数据库名
DB_USER=course_buddy_user      # 应用数据库用户
DB_PASSWORD=changeme           # 应用数据库密码
MYSQL_ROOT_PASSWORD=rootpass   # MySQL root 密码

# ============ Redis ============
REDIS_HOST=redis               # 容器内主机名（docker-compose 中）
REDIS_PORT=6379

# ============ JWT ============
JWT_SECRET=<至少32字符的随机字符串>

# ============ MinIO 文件存储 ============
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET=course-buddy      # 存储桶名称

# ============ 讯飞星火 AI ============
XUNFEI_APP_ID=<你的 AppID>
XUNFEI_API_KEY=<你的 APIKey>
XUNFEI_API_SECRET=<你的 APISecret>
```

---

## 常见问题

### 问题 1：Docker 内存不足

```bash
# 查看容器内存使用
docker stats

# 在 Docker Desktop 中增加内存分配：
# Settings → Resources → Memory → 调整为 4GB+
```

### 问题 2：应用容器不断重启

```bash
# 查看详细错误
docker compose logs app

# 常见原因：
# 1. MySQL 还未完全启动（等待 healthcheck）
# 2. 环境变量未正确设置
# 3. 应用配置错误
```

### 问题 3：端口已被占用

```bash
# 查看端口占用
lsof -i :3306   # MySQL
lsof -i :6379   # Redis
lsof -i :8080   # 应用

# 停止本地同类服务
sudo systemctl stop mysql
sudo systemctl stop redis-server
```

### 问题 4：镜像拉取超时

配置 Docker 镜像加速：

```json
// Docker Desktop → Settings → Docker Engine，添加：
{
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com",
    "https://hub-mirror.c.163.com"
  ]
}
```

### 问题 5：Flyway 迁移冲突

```bash
# 进入 MySQL 容器，清除迁移历史
docker compose exec mysql mysql -u root -prootpassword_dev course_buddy \
  -e "DROP TABLE IF EXISTS flyway_schema_history;"

# 重启应用
docker compose restart app
```

---

## Dockerfile 说明

项目 Dockerfile 采用多阶段构建：

```dockerfile
# 第一阶段：编译构建
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline    # 先缓存依赖层
COPY src ./src
RUN mvn package -DskipTests

# 第二阶段：运行镜像（轻量）
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 下一步

- [API 端点概览](../../../技术文档/API文档/API端点概览.md)
- [环境变量参考](../../../运维文档/部署指南/环境变量参考.md)
- [Docker 生产部署指南](../../../运维文档/部署指南/Docker部署指南.md)
