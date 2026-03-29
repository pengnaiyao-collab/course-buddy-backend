# Docker Compose 部署指南

## 1. 概述

本文档详细介绍使用 Docker Compose 完整部署 Course Buddy Backend 的全部服务，包括应用服务（Spring Boot）、数据库（MySQL 8.0）、缓存（Redis 7.2）和反向代理（Nginx）。

---

## 2. 服务架构

```
外部请求
    │
    ▼ Port 80 / 443
┌─────────────────┐
│  Nginx (nginx)  │  反向代理，处理 HTTP/HTTPS
└────────┬────────┘
         │ proxy_pass http://app:8080
         ▼
┌─────────────────┐
│   App (app)     │  Spring Boot 8080
└────────┬────────┘
    ├────┤ Port 3306
    │   ▼
    │ ┌─────────────────┐
    │ │  MySQL (mysql)  │  主数据库
    │ └─────────────────┘
    │
    └────┤ Port 6379
        ▼
        ┌─────────────────┐
        │  Redis (redis)  │  缓存服务
        └─────────────────┘

（MinIO 可选，单独部署或使用外部 S3）
```

---

## 3. 项目文件结构

```
course-buddy-backend/
├── docker-compose.yml         # 主编排文件
├── docker-compose.override.yml # 本地开发覆盖（可选）
├── .env                       # 环境变量（不提交到 Git）
├── .env.example               # 环境变量示例（提交到 Git）
├── Dockerfile                 # 应用镜像构建文件
├── nginx/
│   ├── nginx.conf             # Nginx 主配置
│   └── conf.d/
│       └── default.conf       # 站点配置
└── src/                       # 源代码
```

---

## 4. docker-compose.yml 详解

```yaml
version: '3.8'

services:
  # ─────────────────────────────────────────
  # MySQL 数据库服务
  # ─────────────────────────────────────────
  mysql:
    image: mysql:8.0
    container_name: course-buddy-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${DB_NAME:-course_buddy}
      MYSQL_USER: ${DB_USER}
      MYSQL_PASSWORD: ${DB_PASSWORD}
      # 设置时区
      TZ: Asia/Shanghai
    ports:
      # 仅内部访问时可注释掉此行（不暴露到宿主机）
      - "3306:3306"
    volumes:
      # 持久化数据目录
      - mysql_data:/var/lib/mysql
      # 自定义 MySQL 配置
      - ./mysql/conf.d:/etc/mysql/conf.d:ro
    command: >
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --default-authentication-plugin=mysql_native_password
      --max_connections=500
      --innodb_buffer_pool_size=256M
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost",
             "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 30s
    networks:
      - backend-network

  # ─────────────────────────────────────────
  # Redis 缓存服务
  # ─────────────────────────────────────────
  redis:
    image: redis:7.2-alpine
    container_name: course-buddy-redis
    restart: unless-stopped
    command: >
      redis-server
      --requirepass ${REDIS_PASSWORD:-}
      --maxmemory 256mb
      --maxmemory-policy allkeys-lru
      --appendonly yes
      --appendfsync everysec
    ports:
      # 生产环境建议注释掉，不暴露 Redis 端口
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - backend-network

  # ─────────────────────────────────────────
  # Spring Boot 应用服务
  # ─────────────────────────────────────────
  app:
    build:
      context: .
      dockerfile: Dockerfile
    image: course-buddy-backend:latest
    container_name: course-buddy-app
    restart: unless-stopped
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    environment:
      # 数据库配置
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: ${DB_NAME:-course_buddy}
      DB_USER: ${DB_USER}
      DB_PASSWORD: ${DB_PASSWORD}
      # Redis 配置
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASSWORD:-}
      # JWT 配置
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: ${JWT_EXPIRATION:-86400000}
      # MinIO 配置
      MINIO_ENDPOINT: ${MINIO_ENDPOINT}
      MINIO_ACCESS_KEY: ${MINIO_ACCESS_KEY}
      MINIO_SECRET_KEY: ${MINIO_SECRET_KEY}
      # 讯飞 AI 配置
      XUNFEI_APP_ID: ${XUNFEI_APP_ID:-}
      XUNFEI_API_KEY: ${XUNFEI_API_KEY:-}
      XUNFEI_API_SECRET: ${XUNFEI_API_SECRET:-}
      # Spring 配置
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-prod}
      TZ: Asia/Shanghai
      JAVA_OPTS: "-Xmx512m -Xms256m -XX:+UseContainerSupport"
    ports:
      # 仅内部访问，由 Nginx 代理
      - "8080:8080"
    volumes:
      # 日志持久化
      - app_logs:/app/logs
    healthcheck:
      test: ["CMD-SHELL", "wget -qO- http://localhost:8080/api/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - backend-network

  # ─────────────────────────────────────────
  # Nginx 反向代理服务
  # ─────────────────────────────────────────
  nginx:
    image: nginx:alpine
    container_name: course-buddy-nginx
    restart: unless-stopped
    depends_on:
      app:
        condition: service_healthy
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/conf.d:/etc/nginx/conf.d:ro
      # SSL 证书（生产环境）
      - ./nginx/ssl:/etc/nginx/ssl:ro
      # 静态文件（如有）
      - nginx_static:/usr/share/nginx/html:ro
    networks:
      - backend-network

# ─────────────────────────────────────────
# 数据卷（持久化）
# ─────────────────────────────────────────
volumes:
  mysql_data:
    driver: local
  redis_data:
    driver: local
  app_logs:
    driver: local
  nginx_static:
    driver: local

# ─────────────────────────────────────────
# 网络（内部隔离）
# ─────────────────────────────────────────
networks:
  backend-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

---

## 5. Nginx 配置

### 5.1 主配置（nginx/nginx.conf）

```nginx
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
    use epoll;
    multi_accept on;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # 日志格式
    log_format main '$remote_addr - $remote_user [$time_local] '
                    '"$request" $status $body_bytes_sent '
                    '"$http_referer" "$http_user_agent"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    keepalive_timeout 65;
    gzip on;
    gzip_types text/plain application/json application/javascript text/css;
    client_max_body_size 100m;  # 支持大文件上传

    include /etc/nginx/conf.d/*.conf;
}
```

### 5.2 站点配置（nginx/conf.d/default.conf）

```nginx
# HTTP → HTTPS 重定向（生产环境）
server {
    listen 80;
    server_name _;
    return 301 https://$host$request_uri;
}

# HTTPS 服务
server {
    listen 443 ssl http2;
    server_name your-domain.com;  # 替换为实际域名

    ssl_certificate     /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # API 代理
    location /api/ {
        proxy_pass http://app:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout 60s;
        proxy_send_timeout 60s;
    }

    # WebSocket 代理
    location /api/ws/ {
        proxy_pass http://app:8080/api/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_read_timeout 3600s;
        proxy_send_timeout 3600s;
    }

    # 健康检查端点（不记录日志）
    location /api/actuator/health {
        proxy_pass http://app:8080/api/actuator/health;
        access_log off;
    }
}
```

---

## 6. 部署步骤

### 6.1 准备环境变量

```bash
# 克隆项目
git clone https://github.com/your-org/course-buddy-backend.git
cd course-buddy-backend

# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件（填入真实配置）
vim .env
```

**.env 文件内容**：
```env
# 数据库配置
MYSQL_ROOT_PASSWORD=StrongRootPass123!
DB_NAME=course_buddy
DB_USER=course_buddy_user
DB_PASSWORD=AppDbPass456!

# Redis 配置（生产环境必须设置密码）
REDIS_PASSWORD=RedisPass789!

# JWT 配置（最少 32 字符的随机字符串）
JWT_SECRET=your-very-secure-jwt-secret-key-minimum-32-characters-long
JWT_EXPIRATION=86400000

# MinIO 配置
MINIO_ENDPOINT=http://your-minio-host:9000
MINIO_ACCESS_KEY=your-minio-access-key
MINIO_SECRET_KEY=your-minio-secret-key

# 讯飞 AI 配置（选填）
XUNFEI_APP_ID=your_app_id
XUNFEI_API_KEY=your_api_key
XUNFEI_API_SECRET=your_api_secret

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

### 6.2 启动所有服务

```bash
# 方式一：后台启动所有服务（自动构建镜像）
docker compose up -d --build

# 方式二：仅启动，不重新构建（使用已有镜像）
docker compose up -d

# 方式三：前台运行（调试时使用）
docker compose up

# 方式四：仅启动特定服务
docker compose up -d mysql redis
docker compose up -d app nginx
```

### 6.3 验证服务状态

```bash
# 查看所有服务状态
docker compose ps

# 预期输出：
# NAME                   STATUS          PORTS
# course-buddy-mysql     Up (healthy)    3306/tcp
# course-buddy-redis     Up (healthy)    6379/tcp
# course-buddy-app       Up (healthy)    0.0.0.0:8080->8080/tcp
# course-buddy-nginx     Up              0.0.0.0:80->80/tcp, 0.0.0.0:443->443/tcp

# 验证 API 可用性
curl http://localhost/api/actuator/health

# 查看各服务日志
docker compose logs -f app        # 应用日志
docker compose logs -f mysql      # 数据库日志
docker compose logs -f redis      # Redis 日志
docker compose logs -f nginx      # Nginx 日志
```

### 6.4 停止服务

```bash
# 停止所有服务（保留数据卷）
docker compose down

# 停止并删除所有数据（慎用！数据将丢失）
docker compose down -v

# 仅停止特定服务
docker compose stop app nginx
```

---

## 7. 更新部署

### 7.1 更新应用代码

```bash
# 拉取最新代码
git pull origin main

# 重新构建并重启应用（不影响 MySQL 和 Redis）
docker compose up -d --build app

# 等待应用健康
docker compose ps app
```

### 7.2 滚动更新（无停机）

```bash
# 构建新镜像
docker compose build app

# 优雅停止旧容器（等待请求处理完）
docker compose stop app

# 启动新容器
docker compose up -d app

# 验证新版本
curl http://localhost/api/actuator/health
```

---

## 8. 数据备份与恢复

### 8.1 备份 MySQL 数据

```bash
# 使用 docker compose exec 执行 mysqldump
docker compose exec mysql mysqldump \
    -u root \
    -p${MYSQL_ROOT_PASSWORD} \
    --all-databases \
    --single-transaction \
    > backup_$(date +%Y%m%d_%H%M%S).sql

# 压缩备份
docker compose exec mysql mysqldump \
    -u root \
    -p${MYSQL_ROOT_PASSWORD} \
    course_buddy | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz
```

### 8.2 恢复 MySQL 数据

```bash
# 恢复备份（确保 MySQL 服务已启动）
docker compose exec -T mysql mysql \
    -u root \
    -p${MYSQL_ROOT_PASSWORD} \
    course_buddy < backup_20240115_100000.sql

# 从压缩备份恢复
gunzip -c backup_20240115_100000.sql.gz | \
    docker compose exec -T mysql mysql \
    -u root \
    -p${MYSQL_ROOT_PASSWORD} \
    course_buddy
```

### 8.3 备份 Redis 数据

```bash
# 触发 Redis RDB 快照
docker compose exec redis redis-cli BGSAVE

# 复制 RDB 文件到宿主机
docker cp course-buddy-redis:/data/dump.rdb ./redis_backup_$(date +%Y%m%d).rdb
```

---

## 9. 生产环境检查清单

### 9.1 安全检查

```
✅ 修改所有默认密码（MySQL root、Redis、MinIO）
✅ JWT_SECRET 长度至少 32 字符，使用随机生成值
✅ 生产环境关闭 Swagger UI（spring.springdoc.api-docs.enabled=false）
✅ MySQL 和 Redis 端口不暴露到公网（注释掉 docker-compose.yml 中的 ports）
✅ Nginx 配置 HTTPS（SSL/TLS 证书）
✅ 设置防火墙规则，仅允许 80/443 端口公网访问
✅ 定期备份 MySQL 数据
```

### 9.2 性能检查

```
✅ 为 MySQL 服务配置足够内存（推荐 2GB+）
✅ JVM 内存限制与容器内存限制匹配
✅ Nginx 开启 gzip 压缩
✅ Redis 设置 maxmemory 和淘汰策略
✅ MySQL 连接池大小与应用并发量匹配
```

### 9.3 监控检查

```
✅ 配置日志持久化（app_logs 卷）
✅ 容器健康检查正常
✅ 设置容器重启策略（unless-stopped）
✅ 配置告警（CPU、内存、磁盘使用率）
```

---

## 10. 扩展配置

### 10.1 多应用实例（水平扩展）

```bash
# 扩展 app 服务到 3 个实例
docker compose up -d --scale app=3

# 注意：需要修改 nginx 配置使用 upstream 负载均衡
```

```nginx
# nginx/conf.d/default.conf 修改
upstream app_servers {
    server app:8080;
    # Docker Compose scale 模式下自动解析多实例
}

location /api/ {
    proxy_pass http://app_servers/api/;
}
```

---

## 11. 相关文档

- [Docker 部署说明](./Docker部署说明.md)
- [环境变量配置](./环境变量配置.md)
- [常见错误及解决方案](../运维文档/故障排查/常见错误及解决方案.md)
