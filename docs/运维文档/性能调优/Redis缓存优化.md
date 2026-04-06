# Redis 缓存优化指南

本文档针对 课伴 Backend 的 Redis 7.2 缓存策略、Key 命名规范、过期策略和缓存预热方案进行详细说明。

---

## 目录

1. [缓存策略总览](#1-缓存策略总览)
2. [Redis Key 命名规范](#2-redis-key-命名规范)
3. [缓存过期策略](#3-缓存过期策略)
4. [各模块缓存设计](#4-各模块缓存设计)
5. [缓存预热策略](#5-缓存预热策略)
6. [缓存穿透、击穿、雪崩防护](#6-缓存穿透击穿雪崩防护)
7. [Redis 内存管理](#7-redis-内存管理)
8. [缓存监控与告警](#8-缓存监控与告警)
9. [Redis 常用运维命令](#9-redis-常用运维命令)

---

## 1. 缓存策略总览

### 1.1 项目缓存使用场景

课伴 Backend 在以下场景使用 Redis：

| 使用场景 | 缓存模式 | 说明 |
|---------|---------|------|
| JWT Token 黑名单 | 主动存储 | 用户登出时将 Token 加入黑名单，拦截重放攻击 |
| 讯飞 AI 响应缓存 | Cache-Aside | `xunfei.enableCache=true`，相同问题复用答案 |
| 课程列表缓存 | Cache-Aside | 减少数据库压力 |
| 用户 Session 信息 | 主动存储 | 快速校验用户角色和权限 |
| 考勤统计缓存 | Write-Behind | 聚合计算结果缓存 |
| 验证码 | 主动存储 | 短 TTL，防暴力破解 |

### 1.2 Cache-Aside 模式（最常用）

```
读取流程：
  应用 → 查 Redis → 命中 → 返回数据
                 → 未命中 → 查 MySQL → 写入 Redis → 返回数据

写入流程：
  应用 → 更新 MySQL → 删除/更新 Redis 缓存
```

```java
// Spring Boot 集成 Redis Cache
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();  // 防止缓存 null 值导致穿透

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        // 课程详情：5 分钟
        cacheConfigs.put("course:detail",
            defaultConfig.entryTtl(Duration.ofMinutes(5)));
        // 用户信息：30 分钟
        cacheConfigs.put("user:profile",
            defaultConfig.entryTtl(Duration.ofMinutes(30)));
        // AI 响应：1 小时（与 xunfei.cacheTtl=3600 对应）
        cacheConfigs.put("ai:response",
            defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(factory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}
```

---

## 2. Redis Key 命名规范

### 2.1 命名格式

```
{项目前缀}:{业务模块}:{数据类型}:{唯一标识}
```

### 2.2 项目 Key 命名规范表

| 业务场景 | Key 格式 | 示例 | TTL |
|---------|---------|------|-----|
| JWT 黑名单 | `cb:auth:blacklist:{jti}` | `cb:auth:blacklist:abc123` | Token 剩余有效期 |
| 用户信息缓存 | `cb:user:profile:{userId}` | `cb:user:profile:42` | 30 分钟 |
| 课程详情 | `cb:course:detail:{courseId}` | `cb:course:detail:7` | 5 分钟 |
| 课程列表（分页） | `cb:course:list:{page}:{size}:{status}` | `cb:course:list:1:10:ACTIVE` | 10 分钟 |
| AI 响应缓存 | `cb:ai:xunfei:{promptHash}` | `cb:ai:xunfei:md5_abc` | 1 小时 |
| 考勤统计 | `cb:attendance:stats:{courseId}` | `cb:attendance:stats:3` | 1 小时 |
| 验证码 | `cb:captcha:{sessionId}` | `cb:captcha:session_x` | 5 分钟 |
| 速率限制 | `cb:ratelimit:xunfei:{userId}` | `cb:ratelimit:xunfei:5` | 1 分钟滑动窗口 |

### 2.3 命名注意事项

```
✅ 使用冒号 (:) 分隔层级，Redis Desktop Manager 等工具会显示为树形结构
✅ 统一使用小写字母和数字
✅ 避免在 Key 中存储密码、Token 明文等敏感信息
❌ 不要使用空格、特殊字符（除 : 和 _）
❌ Key 不宜过长（建议 < 100 字符），否则影响内存效率
❌ 不要使用 cb:* 或 cb:user:* 等模糊模式作为 Key（用 SET/HASH 替代）
```

---

## 3. 缓存过期策略

### 3.1 过期时间设计原则

```
过期时间 = MAX(数据变更频率, 数据新鲜度要求)
```

| 数据类型 | 过期时间 | 原因 |
|---------|---------|------|
| JWT 黑名单 | Token 剩余有效期（最长 24h） | 与 Token 生命周期一致 |
| 用户 Profile | 30 分钟 | 用户信息变更不频繁，但需要相对新鲜 |
| 课程详情 | 5 分钟 | 课程内容可能更新，5 分钟可接受 |
| 已发布课程列表 | 10 分钟 | 新课程发布延迟可接受 |
| AI 响应 | 1 小时 | 同一问题答案稳定，复用率高 |
| 讯飞速率限制 | 60 秒（滑动窗口） | 与 `rateLimitPerMinute=60` 对应 |

### 3.2 Redis 过期策略配置

Redis 7.2 默认使用**惰性过期 + 定期过期**混合策略：

```bash
# redis.conf（生产环境建议显式配置）
# 内存淘汰策略：当内存不足时的行为
maxmemory-policy allkeys-lru    # 推荐：LRU 淘汰所有 Key（含无 TTL 的 Key）
# 或
maxmemory-policy volatile-lru   # 只淘汰有 TTL 的 Key（更保守）

# 设置最大内存（根据服务器配置调整）
maxmemory 256mb
```

在 `docker-compose.yml` 中配置：

```yaml
services:
  redis:
    image: redis:7.2-alpine
    command: >
      redis-server
      --maxmemory 256mb
      --maxmemory-policy allkeys-lru
      --save 900 1
      --save 300 10
      --appendonly yes
      --appendfsync everysec
```

### 3.3 缓存数据一致性

```java
// ✅ 更新数据库后，主动删除缓存（Cache-Aside 写模式）
@Transactional
public void updateCourseInfo(Long courseId, UpdateCourseRequest request) {
    // 1. 更新数据库
    Course course = courseMapper.selectById(courseId);
    // ... 更新字段
    courseMapper.updateById(course);

    // 2. 删除缓存（下次查询时自动重建）
    String cacheKey = "cb:course:detail:" + courseId;
    redisTemplate.delete(cacheKey);

    // 3. 同时清除可能的列表缓存（使用 SCAN 避免阻塞 Redis）
    try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
            .getConnection()
            .scan(ScanOptions.scanOptions().match("cb:course:list:*").count(100).build())) {
        List<String> keysToDelete = new ArrayList<>();
        cursor.forEachRemaining(k -> keysToDelete.add(new String(k, StandardCharsets.UTF_8)));
        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }
    }
}
```

> **注意**：生产环境禁止使用 `KEYS` 命令扫描匹配的 Key，因为它会阻塞 Redis。应始终使用 `SCAN` 命令（非阻塞，分批返回）或通过 Set 数据结构维护 Key 标签集合来批量删除缓存。

---

## 4. 各模块缓存设计

### 4.1 认证模块（Auth）

```java
// JWT Token 黑名单（用户主动登出）
public void logout(String token) {
    long remainingMs = jwtService.getRemainingValidity(token);
    String jti = jwtService.extractJti(token);
    if (remainingMs > 0) {
        redisTemplate.opsForValue().set(
            "cb:auth:blacklist:" + jti,
            "1",
            Duration.ofMillis(remainingMs)
        );
    }
}

// 拦截器中校验黑名单
public boolean isTokenBlacklisted(String jti) {
    return Boolean.TRUE.equals(redisTemplate.hasKey("cb:auth:blacklist:" + jti));
}
```

### 4.2 讯飞 AI 模块（XunFei）

```yaml
# application.yml 中已配置
xunfei:
  enableCache: true      # 开启缓存
  cacheTtl: 3600         # 1 小时
  rateLimitPerMinute: 60 # 每分钟限制 60 次
```

```java
// AI 响应缓存（基于问题内容 Hash）
public String getAiResponse(String prompt) {
    String cacheKey = "cb:ai:xunfei:" + DigestUtils.md5Hex(prompt);

    // 先查缓存
    String cached = (String) redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) return cached;

    // 调用 XunFei API
    String response = xunFeiClient.chat(prompt);

    // 写入缓存
    redisTemplate.opsForValue().set(cacheKey, response, Duration.ofSeconds(3600));
    return response;
}

// 速率限制（滑动窗口）
public boolean checkRateLimit(Long userId) {
    String key = "cb:ratelimit:xunfei:" + userId;
    Long count = redisTemplate.opsForValue().increment(key);
    if (count == 1) {
        redisTemplate.expire(key, Duration.ofMinutes(1));
    }
    return count <= 60; // rateLimitPerMinute=60
}
```



## 5. 缓存预热策略

### 5.1 应用启动时预热

```java
@Component
public class CacheWarmupRunner implements ApplicationRunner {

    @Autowired private ICourseService courseService;
    @Autowired private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始缓存预热...");

        // 预热热门课程列表（前 3 页）
        warmupCourseList();

        log.info("缓存预热完成");
    }

    private void warmupCourseList() {
        try {
            // 预热已发布课程的前 3 页
            for (int page = 1; page <= 3; page++) {
                courseService.listActiveCourses(page, 10);
                // 此方法内部使用 @Cacheable，调用后自动写入 Redis
            }
            log.info("课程列表缓存预热完成（前 3 页）");
        } catch (Exception e) {
            log.warn("课程列表缓存预热失败，将使用数据库兜底: {}", e.getMessage());
        }
    }
}
```

### 5.2 定时刷新缓存

```java
@Component
public class CacheRefreshScheduler {

    // 每 5 分钟刷新一次热门课程缓存
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void refreshCourseListCache() {
        // 先删除旧缓存，触发下次查询时重建
        Set<String> keys = redisTemplate.keys("cb:course:list:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        // 主动预热第一页
        courseService.listActiveCourses(1, 10);
    }
}
```

### 5.3 灰度预热（上线前）

```bash
# 使用脚本预先加载热点数据到 Redis
#!/bin/bash
# warmup.sh - 在部署新版本前执行

BASE_URL="http://localhost:8080/api"
ADMIN_TOKEN="Bearer <admin_jwt_token>"

echo "开始预热课程列表缓存..."
for page in 1 2 3 4 5; do
    curl -s -H "Authorization: $ADMIN_TOKEN" \
         "$BASE_URL/courses?page=$page&size=10&status=ACTIVE" > /dev/null
    echo "  预热第 $page 页完成"
done

echo "预热完成"
```

---

## 6. 缓存穿透、击穿、雪崩防护

### 6.1 缓存穿透（查询不存在的数据）

**问题**：大量请求查询数据库中不存在的 Key（如非法课程 ID），每次都穿透到数据库。

```java
// ✅ 方案一：缓存 null 值（短 TTL）
@Cacheable(value = "course:detail", key = "#courseId",
           unless = "#result == null")
public CourseVO getCourseDetail(Long courseId) {
    CourseVO course = courseMapper.selectById(courseId);
    if (course == null) {
        // 缓存 null 占位符，30 秒后过期
        redisTemplate.opsForValue().set(
            "cb:course:detail:" + courseId, "NULL", Duration.ofSeconds(30));
    }
    return course;
}

// ✅ 方案二：布隆过滤器（推荐用于大规模场景）
// 使用 Redisson BloomFilter
@Bean
public RBloomFilter<Long> courseBloomFilter(RedissonClient redissonClient) {
    RBloomFilter<Long> bloomFilter =
        redissonClient.getBloomFilter("cb:bloom:course");
    bloomFilter.tryInit(100000L, 0.01);  // 预计 10 万课程，误判率 1%
    return bloomFilter;
}
```

### 6.2 缓存击穿（热点 Key 过期）

**问题**：热点 Key（如首页热门课程）过期瞬间，大量并发请求同时查询数据库。

```java
// ✅ 使用分布式锁（Redisson）防止缓存击穿
public CourseVO getCourseDetailWithLock(Long courseId) {
    String cacheKey = "cb:course:detail:" + courseId;

    // 先查缓存
    CourseVO cached = (CourseVO) redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) return cached;

    // 缓存未命中，加分布式锁
    String lockKey = "cb:lock:course:" + courseId;
    RLock lock = redissonClient.getLock(lockKey);
    try {
        if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
            // 双重检查（加锁后再查一次缓存）
            cached = (CourseVO) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) return cached;

            // 查数据库并写缓存
            CourseVO course = courseMapper.selectById(courseId);
            redisTemplate.opsForValue().set(cacheKey, course, Duration.ofMinutes(5));
            return course;
        }
    } finally {
        if (lock.isHeldByCurrentThread()) lock.unlock();
    }
    // 锁超时，降级查数据库
    return courseMapper.selectById(courseId);
}
```

### 6.3 缓存雪崩（大量 Key 同时过期）

**问题**：大量 Key 在同一时刻过期，导致数据库瞬时压力剧增。

```java
// ✅ 过期时间加随机抖动
private Duration randomTtl(int baseTtlMinutes) {
    // 基础 TTL + 随机 0-5 分钟
    int jitter = ThreadLocalRandom.current().nextInt(5);
    return Duration.ofMinutes(baseTtlMinutes + jitter);
}

// 批量写入缓存时使用随机 TTL
courseList.forEach(course -> {
    redisTemplate.opsForValue().set(
        "cb:course:detail:" + course.getId(),
        course,
        randomTtl(5)   // 5-10 分钟随机过期
    );
});
```

---

## 7. Redis 内存管理

### 7.1 内存使用监控

```bash
# 进入 Redis 容器
docker exec -it course-buddy-redis redis-cli

# 查看内存使用详情
127.0.0.1:6379> INFO memory

# 关键指标：
# used_memory_human:     当前使用内存（人类可读）
# used_memory_peak_human: 内存使用峰值
# mem_fragmentation_ratio: 内存碎片率（>1.5 需关注，>2 需碎片整理）
# maxmemory_human:        最大内存限制

# 查看 Key 数量
127.0.0.1:6379> DBSIZE

# 查看 Key 的内存占用（按前缀统计）
127.0.0.1:6379> MEMORY USAGE cb:course:detail:1
```

### 7.2 内存碎片整理

```bash
# Redis 4.0+ 支持在线碎片整理
127.0.0.1:6379> CONFIG SET activedefrag yes
127.0.0.1:6379> CONFIG SET active-defrag-ignore-bytes 100mb
127.0.0.1:6379> CONFIG SET active-defrag-enabled yes
```

### 7.3 数据类型选择

| 场景 | 推荐数据类型 | 原因 |
|------|------------|------|
| 单个对象缓存 | String (JSON) | 简单，Spring Cache 默认 |
| 用户 Session 多字段 | Hash | 可单独更新某个字段 |
| 速率限制计数 | String (INCR) | 原子操作 |
| 在线用户集合 | Set | 去重，支持集合运算 |
| 排行榜（课程热度） | Sorted Set | 带分数排序 |

---

## 8. 缓存监控与告警

### 8.1 关键监控指标

```bash
# 缓存命中率（重要指标，建议 > 80%）
127.0.0.1:6379> INFO stats
# keyspace_hits / (keyspace_hits + keyspace_misses)

# 连接数
127.0.0.1:6379> INFO clients
# connected_clients 建议 < 100（当前 HikariCP max=5，预留空间足够）

# 慢命令日志（执行时间 > 10ms 的命令）
127.0.0.1:6379> CONFIG SET slowlog-log-slower-than 10000
127.0.0.1:6379> SLOWLOG GET 10
```

### 8.2 Spring Boot Actuator 缓存监控

```bash
# 查看所有缓存名称
GET /api/actuator/caches

# 清除指定缓存（需开启 caches endpoint）
DELETE /api/actuator/caches/course:detail
```

---

## 9. Redis 常用运维命令

```bash
# 连接 Docker 中的 Redis
docker exec -it course-buddy-redis redis-cli

# 查看所有 course-buddy 相关的 Key（生产环境禁用 KEYS，改用 SCAN）
127.0.0.1:6379> SCAN 0 MATCH "cb:*" COUNT 100

# 查看 Key 的 TTL（-1 永不过期，-2 不存在）
127.0.0.1:6379> TTL cb:course:detail:1

# 手动删除指定 Key（紧急清除缓存时使用）
127.0.0.1:6379> DEL cb:course:detail:1

# 批量删除 course 相关缓存（使用 SCAN 避免阻塞）
127.0.0.1:6379> EVAL "local keys = redis.call('SCAN', '0', 'MATCH', 'cb:course:*', 'COUNT', '100') \
                       for _, key in ipairs(keys[2]) do redis.call('DEL', key) end" 0

# 查看 Redis 实时命令（调试用，生产慎用）
127.0.0.1:6379> MONITOR

# 持久化检查
127.0.0.1:6379> BGSAVE        # 触发 RDB 快照
127.0.0.1:6379> LASTSAVE      # 上次保存时间戳
127.0.0.1:6379> BGREWRITEAOF  # 触发 AOF 重写
```

---

## 参考资料

- [Redis 7.2 官方文档](https://redis.io/docs/)
- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)
- [Redisson 分布式锁](https://github.com/redisson/redisson/wiki/8.-Distributed-locks-and-synchronizers)
- [Redis 内存优化最佳实践](https://redis.io/docs/management/optimization/memory-optimization/)
