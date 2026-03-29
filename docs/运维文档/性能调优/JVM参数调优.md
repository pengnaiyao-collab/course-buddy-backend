# JVM 参数调优指南

本文档针对 Course Buddy Backend（Spring Boot 3.2.2 / Java 17 / eclipse-temurin:17-jre-alpine）在容器化环境中的 JVM 调优实践。

---

## 目录

1. [容器感知 JVM 设置](#1-容器感知-jvm-设置)
2. [当前 Dockerfile 配置解析](#2-当前-dockerfile-配置解析)
3. [GC 调优策略](#3-gc-调优策略)
4. [内存设置与堆分析](#4-内存设置与堆分析)
5. [线程与并发调优](#5-线程与并发调优)
6. [JVM 监控（Actuator + JMX）](#6-jvm-监控actuator--jmx)
7. [生产环境推荐配置](#7-生产环境推荐配置)
8. [调优验证与基准测试](#8-调优验证与基准测试)
9. [常见 JVM 问题排查](#9-常见-jvm-问题排查)

---

## 1. 容器感知 JVM 设置

### 1.1 为什么需要容器感知

Java 8u131 之前，JVM 无法识别容器的 cgroup 内存限制，会读取宿主机的全部内存作为 `-Xmx` 的计算基础，导致 OOM Kill。从 Java 10 起（Java 8u131+ 已向后移植），`-XX:+UseContainerSupport` 默认启用，JVM 会读取 cgroup 内存配额。

### 1.2 当前项目已启用的参数

```dockerfile
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

| 参数 | 说明 |
|------|------|
| `-XX:+UseContainerSupport` | 启用容器感知，从 cgroup 读取内存限制（Java 17 默认已启用，显式声明更安全） |
| `-XX:MaxRAMPercentage=75.0` | 堆最大内存 = 容器内存限制 × 75%，剩余 25% 留给 Metaspace、线程栈、堆外内存 |
| `-Djava.security.egd=file:/dev/./urandom` | 使用非阻塞熵源，避免 SecureRandom 阻塞，加速 JWT 生成（对 `/api/auth/**` 接口有明显影响） |

### 1.3 容器内存分配建议

| 容器内存限制 | MaxRAMPercentage=75% 下的堆大小 | 适用场景 |
|-------------|--------------------------------|---------|
| 512 MB | ~384 MB | 开发/测试环境 |
| 1 GB | ~768 MB | 小规模生产（≤50 并发） |
| 2 GB | ~1536 MB | 中规模生产（≤200 并发） |
| 4 GB | ~3 GB | 大规模生产（500+ 并发） |

> **注意**：HikariCP 当前配置 `maximum-pool-size: 5`（dev），每个连接约占用 1-2 MB 直接内存，生产环境调大连接池时需相应增加容器内存。

---

## 2. 当前 Dockerfile 配置解析

### 2.1 基础镜像选型

```dockerfile
FROM eclipse-temurin:17-jre-alpine
```

- `eclipse-temurin`：Adoptium 官方发行版，安全更新及时
- `17-jre`：仅包含 JRE（不含 JDK 开发工具），镜像体积更小
- `alpine`：基于 musl libc，镜像约 80-100 MB（vs Debian slim 约 200 MB）

> **注意**：Alpine 使用 musl libc，部分依赖 glibc 的 JNI 库（如某些加密库）可能不兼容。当前项目无此问题。

### 2.2 安全配置

```dockerfile
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
```

以非 root 用户运行，符合最小权限原则。

---

## 3. GC 调优策略

### 3.1 Java 17 默认 GC：G1GC

Java 17 默认使用 G1 GC（Garbage First），适合低延迟、大堆场景。Course Buddy 以 Web API 请求为主，G1GC 是合适的默认选择。

```bash
# 验证当前使用的 GC
docker exec course-buddy-app java -XX:+PrintCommandLineFlags -version 2>&1 | grep GC
```

### 3.2 G1GC 推荐参数

```bash
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200 \
-XX:G1HeapRegionSize=4m \
-XX:G1NewSizePercent=20 \
-XX:G1MaxNewSizePercent=40 \
-XX:G1MixedGCCountTarget=8 \
-XX:InitiatingHeapOccupancyPercent=45
```

参数说明：

| 参数 | 建议值 | 说明 |
|------|--------|------|
| `MaxGCPauseMillis` | 200 | 目标最大 STW 停顿时间（毫秒），API 接口 P99 < 500ms 时可设 200 |
| `G1HeapRegionSize` | 4m | 堆区域大小，推荐：堆 ≤ 1GB 用 2m，1-4GB 用 4m，>4GB 用 8m |
| `G1NewSizePercent` | 20 | 新生代占堆的最小比例，Spring Boot 应用对象分配快 |
| `InitiatingHeapOccupancyPercent` | 45 | 触发混合 GC 的堆使用率阈值，降低可减少 Full GC 风险 |

### 3.3 ZGC（低延迟场景）

若对延迟要求极高（P99 < 50ms），可切换至 ZGC：

```bash
-XX:+UseZGC \
-XX:+ZGenerational \
-XX:SoftMaxHeapSize=1g
```

> ZGC 在 Java 21 中已成熟，Java 17 中仍为实验性功能，生产环境谨慎使用。

### 3.4 GC 日志配置

```bash
-Xlog:gc*:file=/app/logs/gc.log:time,uptime,level,tags:filecount=5,filesize=20m
```

在 Dockerfile 的 ENTRYPOINT 中追加此参数，日志按 5 个文件、每文件 20MB 滚动。

---

## 4. 内存设置与堆分析

### 4.1 堆内存组成

```
总内存 = 堆内存(Heap) + 非堆内存(Non-Heap) + 直接内存(Direct Memory)
```

| 区域 | 包含内容 | 调优参数 |
|------|---------|---------|
| 堆内存 | 新生代 + 老年代，业务对象 | `-Xms`, `-Xmx`, `MaxRAMPercentage` |
| Metaspace | 类元数据（替代 PermGen） | `-XX:MetaspaceSize`, `-XX:MaxMetaspaceSize` |
| 线程栈 | 每个线程约 512KB-1MB | `-Xss` |
| 直接内存 | NIO 缓冲区、Netty、JDBC | `-XX:MaxDirectMemorySize` |

### 4.2 Spring Boot 应用典型内存分布

对于 Course Buddy（含 MyBatis-Plus、Redis、MinIO、WebSocket 等组件）：

```
启动后稳定内存（容器 1GB 时）：
  堆已用：  ~150-250 MB（业务对象、缓存）
  Metaspace：~80-120 MB（大量框架类、动态代理）
  线程栈：  ~30-50 MB（Tomcat 线程池默认 200 线程 × 512KB）
  直接内存：~20-40 MB（Netty、NIO Buffer）
  合计：    ~280-460 MB
```

### 4.3 推荐的初始堆大小参数

```bash
# 避免堆动态扩容导致的 Full GC，初始堆设为最大堆的一半
-XX:InitialRAMPercentage=50.0 \
-XX:MaxRAMPercentage=75.0
```

或使用固定值（容器内存 1GB 时）：

```bash
-Xms512m -Xmx768m
```

### 4.4 Metaspace 调优

Spring Boot 应用类加载量大，建议显式设置 Metaspace：

```bash
-XX:MetaspaceSize=128m \
-XX:MaxMetaspaceSize=256m
```

> 若未设置 `MaxMetaspaceSize`，Metaspace 默认无上限，在类加载异常（如 CGLIB 代理泄漏）时会持续增长直至 OOM。

### 4.5 堆转储分析

发生 OOM 时自动生成堆转储：

```bash
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/app/logs/heapdump.hprof
```

使用 Eclipse MAT 或 VisualVM 分析：

```bash
# 从容器中复制堆转储
docker cp course-buddy-app:/app/logs/heapdump.hprof ./heapdump.hprof
```

---

## 5. 线程与并发调优

### 5.1 Tomcat 线程池

Course Buddy 使用 Spring Boot 内嵌 Tomcat，默认最大 200 个工作线程：

```yaml
# application.yml 中添加
server:
  tomcat:
    threads:
      max: 200          # 最大工作线程数
      min-spare: 20     # 最小空闲线程数
    accept-count: 100   # 等待队列长度
    connection-timeout: 20000
```

### 5.2 HikariCP 连接池与线程协调

```
HikariCP 最优连接数 ≈ CPU 核心数 × 2 + 有效磁盘主轴数
```

Dev 环境设置 5，生产环境建议：

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # 生产环境
      minimum-idle: 5
      connection-timeout: 30000  # 30s
      idle-timeout: 600000       # 10min
      max-lifetime: 1800000      # 30min
```

---

## 6. JVM 监控（Actuator + JMX）

### 6.1 当前 Actuator 配置

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: never
```

### 6.2 开发/监控环境扩展 Actuator

```yaml
# application-dev.yml 中追加
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,threaddump,heapdump,loggers
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

> **警告**：`heapdump` 端点不得在生产环境对外暴露，需通过 Spring Security 或 Nginx ACL 保护。

### 6.3 关键监控指标

```bash
# JVM 堆内存使用
GET /api/actuator/metrics/jvm.memory.used?tag=area:heap

# GC 暂停时间
GET /api/actuator/metrics/jvm.gc.pause

# 活跃线程数
GET /api/actuator/metrics/jvm.threads.live

# HTTP 请求耗时
GET /api/actuator/metrics/http.server.requests

# HikariCP 连接池
GET /api/actuator/metrics/hikaricp.connections.active
GET /api/actuator/metrics/hikaricp.connections.pending
```

### 6.4 健康检查端点

```bash
# 生产环境健康检查（Docker health check 使用）
curl http://localhost:8080/api/actuator/health
# 返回示例：{"status":"UP"}
```

---

## 7. 生产环境推荐配置

### 7.1 完整 ENTRYPOINT（生产环境）

```dockerfile
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:InitialRAMPercentage=50.0", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-XX:G1HeapRegionSize=4m", \
  "-XX:MetaspaceSize=128m", \
  "-XX:MaxMetaspaceSize=256m", \
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-XX:HeapDumpPath=/app/logs/heapdump.hprof", \
  "-Xlog:gc*:file=/app/logs/gc.log:time,uptime,level,tags:filecount=5,filesize=20m", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dfile.encoding=UTF-8", \
  "-Duser.timezone=Asia/Shanghai", \
  "-jar", "app.jar"]
```

### 7.2 docker-compose 内存限制

```yaml
# docker-compose.yml 中 app 服务添加
services:
  app:
    deploy:
      resources:
        limits:
          memory: 1g
          cpus: '1.0'
        reservations:
          memory: 512m
```

---

## 8. 调优验证与基准测试

### 8.1 启动时间基准

```bash
# 观察 Spring Boot 启动日志中的启动时间
docker logs course-buddy-app 2>&1 | grep "Started CourseBuddyApplication"
# 期望值：< 15s（1GB 容器，正常网络）
```

### 8.2 压测时 JVM 监控

使用 `jstat` 监控 GC 活动（需进入容器）：

```bash
docker exec -it course-buddy-app sh
# 查找 Java 进程 PID
PID=$(ps aux | grep java | grep -v grep | awk '{print $1}')
# 每 2 秒输出一次 GC 统计
jstat -gcutil $PID 2000
```

输出字段说明：

| 字段 | 说明 | 警戒值 |
|------|------|--------|
| S0/S1 | Survivor 区使用率 | > 90% 需关注 |
| E | Eden 区使用率 | 正常波动 |
| O | 老年代使用率 | > 80% 需关注 |
| M | Metaspace 使用率 | > 90% 需扩容 |
| YGC | Young GC 次数 | 频率过高（>1次/秒）需调优 |
| FGC | Full GC 次数 | > 0 需立即排查 |

---

## 9. 常见 JVM 问题排查

### 9.1 OutOfMemoryError: Java heap space

**现象**：接口响应变慢，最终 OOM 崩溃

**排查步骤**：
1. 检查 `/app/logs/heapdump.hprof` 是否生成
2. 使用 MAT 分析内存泄漏点（关注 MyBatis-Plus 结果集、文件上传流）
3. 检查 Redis 缓存是否未设过期时间导致对象无法回收

```bash
# 快速查看堆使用情况（无需进入容器）
curl -s http://localhost:8080/api/actuator/metrics/jvm.memory.used?tag=area:heap \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(f\"Heap Used: {d['measurements'][0]['value']/1024/1024:.1f} MB\")"
```

### 9.2 OutOfMemoryError: Metaspace

**原因**：CGLIB 动态代理类过多，或 ClassLoader 泄漏

**解决**：
```bash
# 增加 Metaspace 上限
-XX:MaxMetaspaceSize=512m
# 同时检查是否有框架反复创建代理类
```

### 9.3 GC overhead limit exceeded

**原因**：98% 时间用于 GC，回收内存不足 2%

**解决**：
1. 增加容器内存限制
2. 降低 `InitiatingHeapOccupancyPercent`（如从 45% 降至 35%）
3. 检查大对象分配（MinIO 上传流是否正确关闭）

### 9.4 启动慢（>30s）

**原因**：Flyway 迁移慢、类加载慢

**解决**：
```bash
# 开启类数据共享（AppCDS），减少类加载时间约 20-30%
# 步骤 1：生成类列表
java -XX:DumpLoadedClassList=/app/classes.lst -jar app.jar

# 步骤 2：创建 CDS 归档
java -Xshare:dump -XX:SharedArchiveFile=/app/app.jsa \
     -XX:SharedClassListFile=/app/classes.lst -jar app.jar

# 步骤 3：使用 CDS 启动
java -Xshare:on -XX:SharedArchiveFile=/app/app.jsa -jar app.jar
```

---

## 参考资料

- [Eclipse Temurin 17 Release Notes](https://adoptium.net/temurin/releases/)
- [G1GC Tuning Guide - Oracle](https://docs.oracle.com/en/java/javase/17/gctuning/garbage-first-g1-garbage-collector1.html)
- [Spring Boot Performance Tuning](https://spring.io/blog/2022/10/11/learning-spring-boot-performance-optimizations)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
