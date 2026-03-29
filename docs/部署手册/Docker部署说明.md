# Docker 部署说明

## 1. 概述

本文档介绍如何使用 Docker 单独构建和运行 Course Buddy Backend 应用镜像。如需完整的多服务编排（含 MySQL、Redis、Nginx），请参阅 [Docker Compose 部署](./Docker_Compose部署.md)。

---

## 2. 前置条件

### 2.1 环境要求

| 软件 | 最低版本 | 推荐版本 | 验证命令 |
|------|----------|----------|----------|
| Docker Engine | 20.10 | 24.x+ | `docker --version` |
| Docker Compose | 2.0 | 2.21+ | `docker compose version` |
| 可用内存 | 2 GB | 4 GB+ | - |
| 可用磁盘 | 5 GB | 10 GB+ | - |

### 2.2 验证 Docker 安装

```bash
# 验证 Docker 版本
docker --version
# Docker version 24.0.7, build afdd53b

# 验证 Docker 正常运行
docker run --rm hello-world
```

---

## 3. Dockerfile 详解

项目 Dockerfile 位于仓库根目录：

```dockerfile
# 阶段一：构建
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# 使用 Maven Wrapper 构建（跳过测试加快速度）
RUN ./mvnw package -DskipTests --no-transfer-progress

# 阶段二：运行
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 安全：创建非 root 用户运行应用
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 从构建阶段复制 JAR
COPY --from=builder /app/target/*.jar app.jar

# 切换到非 root 用户
USER appuser

# 应用端口
EXPOSE 8080

# 健康检查（每 30 秒，超时 10 秒，连续 3 次失败才标记不健康）
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/actuator/health || exit 1

# 启动命令（推荐 JVM 参数）
ENTRYPOINT ["java", \
    "-Xmx512m", \
    "-Xms256m", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
```

**关键设计说明**：
- **多阶段构建**：构建镜像仅用于编译，运行镜像仅包含 JRE，镜像体积更小
- **eclipse-temurin:17-jre-alpine**：基于 Alpine Linux 的轻量级 JRE 镜像（约 150MB）
- **非 root 用户**：`appuser` 运行应用，提升安全性
- **UseContainerSupport**：JVM 自动识别容器内存限制（Docker 资源约束）

---

## 4. 构建 Docker 镜像

### 4.1 基础构建

```bash
# 进入项目根目录
cd /home/runner/work/course-buddy-backend/course-buddy-backend

# 构建镜像（标签：course-buddy-backend:latest）
docker build -t course-buddy-backend:latest .

# 构建带版本标签的镜像
docker build -t course-buddy-backend:1.0.0 .
docker build -t course-buddy-backend:latest .
```

### 4.2 多平台构建（ARM/x86 跨平台）

```bash
# 为 M1/M2 Mac 和 Linux x86 同时构建
docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t course-buddy-backend:latest \
    --push .
```

### 4.3 构建参数传递

```bash
# 传递构建参数（如 Maven 配置）
docker build \
    --build-arg MAVEN_OPTS="-Xmx1g" \
    -t course-buddy-backend:latest .
```

### 4.4 查看构建结果

```bash
# 查看镜像
docker images | grep course-buddy

# 查看镜像层信息
docker history course-buddy-backend:latest

# 查看镜像详情
docker inspect course-buddy-backend:latest
```

---

## 5. 运行 Docker 容器

### 5.1 基础运行命令

```bash
docker run -d \
    --name course-buddy-app \
    -p 8080:8080 \
    -e DB_HOST=host.docker.internal \
    -e DB_PORT=3306 \
    -e DB_NAME=course_buddy \
    -e DB_USER=app_user \
    -e DB_PASSWORD=your_db_password \
    -e REDIS_HOST=host.docker.internal \
    -e REDIS_PORT=6379 \
    -e JWT_SECRET=your-256-bit-secret-key-here-minimum-32-chars \
    -e MINIO_ENDPOINT=http://host.docker.internal:9000 \
    -e MINIO_ACCESS_KEY=minioadmin \
    -e MINIO_SECRET_KEY=minioadmin \
    course-buddy-backend:latest
```

**注意**：`host.docker.internal` 用于在 Docker 容器内访问宿主机服务（适用于 Mac/Windows，Linux 需替换为宿主机 IP）。

### 5.2 使用 .env 文件运行

```bash
# 创建 .env 文件（参考 .env.example）
cp .env.example .env
# 编辑 .env 文件填入真实配置
vim .env

# 使用 --env-file 传入环境变量
docker run -d \
    --name course-buddy-app \
    -p 8080:8080 \
    --env-file .env \
    course-buddy-backend:latest
```

### 5.3 挂载配置文件（可选）

```bash
# 挂载自定义 application.yml（覆盖内置配置）
docker run -d \
    --name course-buddy-app \
    -p 8080:8080 \
    --env-file .env \
    -v $(pwd)/config/application-prod.yml:/app/config/application-prod.yml \
    course-buddy-backend:latest
```

### 5.4 指定 Spring Profile

```bash
# 使用生产环境 Profile
docker run -d \
    --name course-buddy-app \
    -p 8080:8080 \
    --env-file .env \
    -e SPRING_PROFILES_ACTIVE=prod \
    course-buddy-backend:latest
```

---

## 6. 健康检查

### 6.1 查看容器状态

```bash
# 查看容器运行状态（包含健康检查结果）
docker ps | grep course-buddy-app
# STATUS 列应显示 "Up X minutes (healthy)"

# 查看健康检查详情
docker inspect --format='{{json .State.Health}}' course-buddy-app | python3 -m json.tool
```

### 6.2 手动测试健康端点

```bash
# 访问 Actuator 健康端点
curl -s http://localhost:8080/api/actuator/health | python3 -m json.tool

# 预期响应：
# {
#     "status": "UP",
#     "components": {
#         "db": {"status": "UP"},
#         "redis": {"status": "UP"},
#         "diskSpace": {"status": "UP"}
#     }
# }
```

### 6.3 等待容器启动

```bash
# 循环等待健康检查通过（最多等待 120 秒）
timeout 120 bash -c '
    until curl -sf http://localhost:8080/api/actuator/health | grep -q "\"status\":\"UP\""; do
        echo "等待应用启动..."
        sleep 5
    done
    echo "应用启动成功！"
'
```

---

## 7. 日志管理

### 7.1 查看日志

```bash
# 实时查看日志
docker logs -f course-buddy-app

# 查看最近 100 行
docker logs --tail 100 course-buddy-app

# 查看指定时间之后的日志
docker logs --since 2024-01-15T10:00:00 course-buddy-app

# 查看指定时间范围的日志
docker logs --since 2024-01-15T10:00:00 --until 2024-01-15T11:00:00 course-buddy-app
```

### 7.2 日志驱动配置（生产环境）

```bash
# 使用 JSON 日志驱动，限制日志大小
docker run -d \
    --name course-buddy-app \
    --log-driver json-file \
    --log-opt max-size=100m \
    --log-opt max-file=5 \
    -p 8080:8080 \
    --env-file .env \
    course-buddy-backend:latest
```

---

## 8. 容器管理

### 8.1 基础操作命令

```bash
# 停止容器
docker stop course-buddy-app

# 启动已停止的容器
docker start course-buddy-app

# 重启容器
docker restart course-buddy-app

# 删除容器（需先停止）
docker stop course-buddy-app && docker rm course-buddy-app

# 进入容器内部（调试用）
docker exec -it course-buddy-app sh

# 查看容器资源使用情况
docker stats course-buddy-app
```

### 8.2 更新应用（零停机）

```bash
# 1. 构建新镜像
docker build -t course-buddy-backend:1.0.1 .

# 2. 停止旧容器
docker stop course-buddy-app

# 3. 删除旧容器（保留镜像以便回滚）
docker rm course-buddy-app

# 4. 使用新镜像启动
docker run -d \
    --name course-buddy-app \
    -p 8080:8080 \
    --env-file .env \
    course-buddy-backend:1.0.1

# 5. 验证新版本健康
curl -s http://localhost:8080/api/actuator/health

# 6. 若失败，回滚到旧版本
docker stop course-buddy-app && docker rm course-buddy-app
docker run -d --name course-buddy-app -p 8080:8080 --env-file .env \
    course-buddy-backend:1.0.0
```

---

## 9. 资源限制（生产环境）

```bash
docker run -d \
    --name course-buddy-app \
    -p 8080:8080 \
    --env-file .env \
    # 内存限制：最大 1GB，预留 512MB
    --memory="1g" \
    --memory-reservation="512m" \
    # CPU 限制：最多使用 1 个 CPU
    --cpus="1.0" \
    # 重启策略：非正常退出时自动重启（最多 3 次）
    --restart=on-failure:3 \
    course-buddy-backend:latest
```

---

## 10. 常见问题排查

### 问题 1：容器启动失败（Exit 1）

```bash
# 查看详细错误日志
docker logs course-buddy-app

# 常见原因：
# - 数据库连接失败：检查 DB_HOST、DB_PORT、DB_USER、DB_PASSWORD
# - Redis 连接失败：检查 REDIS_HOST、REDIS_PORT
# - JWT_SECRET 未配置或太短（需要至少 32 字符）
# - MinIO 无法连接：检查 MINIO_ENDPOINT
```

### 问题 2：容器状态 "unhealthy"

```bash
# 查看健康检查历史
docker inspect --format='{{range .State.Health.Log}}{{.Output}}{{end}}' course-buddy-app

# 手动测试健康端点
docker exec course-buddy-app wget -qO- http://localhost:8080/api/actuator/health
```

### 问题 3：端口 8080 被占用

```bash
# 查看端口占用
lsof -i :8080
# 或
ss -tlnp | grep 8080

# 使用其他端口（容器内仍是 8080）
docker run -d -p 8081:8080 --name course-buddy-app ...
```

### 问题 4：镜像构建失败（Maven 下载超时）

```bash
# 使用本地 Maven 缓存加速构建
docker build \
    -v ~/.m2:/root/.m2 \
    -t course-buddy-backend:latest .

# 或设置 Maven 镜像源
# 在 Dockerfile 中添加 settings.xml 挂载
```

---

## 11. 相关文档

- [Docker Compose 部署](./Docker_Compose部署.md)
- [环境变量配置](./环境变量配置.md)
- [常见错误及解决方案](../运维文档/故障排查/常见错误及解决方案.md)
