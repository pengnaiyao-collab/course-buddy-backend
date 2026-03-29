# MinIO 文件备份指南

本文档针对 Course Buddy Backend 使用的 MinIO 8.6.0 对象存储，提供备份策略、mc 客户端操作、存储生命周期管理和灾难恢复方案。

---

## 目录

1. [MinIO 存储概览](#1-minio-存储概览)
2. [mc（MinIO Client）安装与配置](#2-mcminio-client安装与配置)
3. [备份策略与方案](#3-备份策略与方案)
4. [自动化备份脚本](#4-自动化备份脚本)
5. [存储生命周期管理](#5-存储生命周期管理)
6. [灾难恢复](#6-灾难恢复)
7. [MinIO 数据完整性验证](#7-minio-数据完整性验证)
8. [监控与告警](#8-监控与告警)

---

## 1. MinIO 存储概览

### 1.1 Course Buddy 存储结构

```yaml
# application.yml 中的 MinIO 配置
minio:
  endpoint: ${MINIO_ENDPOINT:http://localhost:9000}
  accessKey: ${MINIO_ACCESS_KEY:minioadmin}
  secretKey: ${MINIO_SECRET_KEY:minioadmin}
  bucketName: ${MINIO_BUCKET:course-buddy}
  maxFileSize: 1073741824   # 最大 1 GB（分块上传）
  chunkSize: 5242880        # 分块大小 5 MB
```

**Bucket 名称**：`course-buddy`

**文件分类**（来自 V13 迁移 `Add_Course_Module_Tables`）：

| 文件类型 | 存储路径前缀 | 说明 |
|---------|------------|------|
| 课程封面图 | `courses/covers/` | 课程封面图片，JPEG/PNG，< 5MB |
| 课程资源 | `courses/resources/{courseId}/` | 课件、PDF、视频等 |
| 笔记附件 | `notes/attachments/{userId}/` | 笔记中上传的图片和文件 |
| 作业附件 | `assignments/{assignmentId}/` | 作业题目附件 |
| 提交文件 | `submissions/{submissionId}/` | 学生提交的作业文件 |
| OCR 图片 | `ocr/images/{userId}/` | OCR 识别的上传图片 |
| 用户头像 | `users/avatars/` | 用户头像 |
| 知识库资源 | `knowledge/{baseId}/` | 知识库附件（V14 新增） |

### 1.2 文件上传方式

Course Buddy 使用分块上传（支持最大 1 GB 文件）：
1. `POST /api/upload/init` - 初始化分块上传，获取 `uploadId`
2. `POST /api/upload/chunk` - 上传各个分块
3. `POST /api/upload/complete` - 合并分块，完成上传

### 1.3 存储规模估算

| 文件类型 | 平均大小 | 预计数量（中型院校） | 预计总量 |
|---------|---------|------------------|---------|
| 课程资源 | 50 MB | 5000 个 | 250 GB |
| 作业提交 | 5 MB | 50000 个 | 250 GB |
| 笔记附件 | 2 MB | 20000 个 | 40 GB |
| OCR 图片 | 1 MB | 10000 个 | 10 GB |
| **合计** | | | **~550 GB** |

---

## 2. mc（MinIO Client）安装与配置

### 2.1 安装 mc

```bash
# Linux (x86_64)
curl -sL https://dl.min.io/client/mc/release/linux-amd64/mc -o /usr/local/bin/mc
chmod +x /usr/local/bin/mc
mc --version

# macOS
brew install minio/stable/mc

# Docker 方式（无需安装）
docker run --rm -it --network course-buddy-network \
  minio/mc [命令]
```

### 2.2 配置 mc 连接

```bash
# 添加 Course Buddy MinIO 实例别名
mc alias set coursebuddy \
  http://localhost:9000 \
  "${MINIO_ACCESS_KEY:-minioadmin}" \
  "${MINIO_SECRET_KEY:-minioadmin}"

# 验证连接
mc admin info coursebuddy

# 列出所有 Bucket
mc ls coursebuddy

# 列出 course-buddy bucket 中的文件
mc ls coursebuddy/course-buddy/
mc ls --recursive coursebuddy/course-buddy/courses/
```

### 2.3 配置备份目标（第二个 MinIO 或云存储）

```bash
# 添加备份目标（另一台 MinIO 服务器）
mc alias set coursebuddy-backup \
  http://backup-server:9000 \
  "${BACKUP_MINIO_ACCESS_KEY}" \
  "${BACKUP_MINIO_SECRET_KEY}"

# 添加 AWS S3 作为备份目标（可选）
mc alias set s3-backup \
  https://s3.amazonaws.com \
  "${AWS_ACCESS_KEY_ID}" \
  "${AWS_SECRET_ACCESS_KEY}"
```

---

## 3. 备份策略与方案

### 3.1 备份方案对比

| 方案 | 工具 | 优点 | 缺点 | 推荐场景 |
|------|------|------|------|---------|
| mc mirror | mc | 简单，增量同步 | 需要目标 MinIO 或 S3 | 异地备份 |
| mc cp | mc | 精确控制 | 全量复制慢 | 特定目录备份 |
| 文件系统备份 | tar/rsync | 速度快 | 需要访问 Docker 卷 | 同机备份 |
| MinIO 站点复制 | MinIO Admin | 实时同步 | 需要 MinIO 企业版或双活部署 | 高可用场景 |

### 3.2 推荐策略

```
每日增量备份（mc mirror）→ 备份 MinIO 实例或 NFS
每周全量备份（tar 压缩）→ 本地磁盘保留 4 周
持续同步（mc mirror --watch）→ 用于关键数据实时备份
```

---

## 4. 自动化备份脚本

### 4.1 每日增量备份脚本

```bash
#!/bin/bash
# MinIO 每日增量备份脚本
# 文件：/opt/course-buddy/scripts/backup_minio.sh

set -euo pipefail

# ===================== 配置区 =====================
SOURCE_ALIAS="coursebuddy"
SOURCE_BUCKET="course-buddy"
BACKUP_ALIAS="coursebuddy-backup"          # 备份目标 MinIO
BACKUP_BUCKET="course-buddy-backup"       # 备份 Bucket
LOCAL_BACKUP_DIR="/opt/backups/course-buddy/minio"
LOG_FILE="${LOCAL_BACKUP_DIR}/backup.log"
RETENTION_DAYS=7
# =================================================

mkdir -p "${LOCAL_BACKUP_DIR}"

TIMESTAMP=$(date +%Y%m%d_%H%M%S)

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "${LOG_FILE}"
}

log "====== 开始 MinIO 备份 ======"

# 方案 A：同步到备份 MinIO（如果有备份实例）
if mc alias list | grep -q "${BACKUP_ALIAS}"; then
    log "执行异地镜像同步..."
    mc mirror \
      --overwrite \
      --remove \
      "${SOURCE_ALIAS}/${SOURCE_BUCKET}" \
      "${BACKUP_ALIAS}/${BACKUP_BUCKET}" \
      >> "${LOG_FILE}" 2>&1 && \
    log "✅ 异地镜像同步完成" || \
    log "⚠️  异地镜像同步失败，将执行本地备份"
fi

# 方案 B：本地文件系统备份
log "执行本地备份..."
BACKUP_FILE="${LOCAL_BACKUP_DIR}/minio_backup_${TIMESTAMP}.tar.gz"

# 从 MinIO 下载所有文件到本地（增量）
LOCAL_MIRROR_DIR="${LOCAL_BACKUP_DIR}/mirror"
mkdir -p "${LOCAL_MIRROR_DIR}"

mc mirror \
  --overwrite \
  "${SOURCE_ALIAS}/${SOURCE_BUCKET}" \
  "${LOCAL_MIRROR_DIR}" \
  >> "${LOG_FILE}" 2>&1

# 每周日打包压缩（减少磁盘碎片）
if [ "$(date +%u)" -eq 7 ]; then
    log "周日全量压缩归档..."
    tar -czf "${BACKUP_FILE}" -C "${LOCAL_MIRROR_DIR}" . && \
    log "✅ 压缩归档完成：$(du -sh ${BACKUP_FILE})"
fi

# 清理旧归档
find "${LOCAL_BACKUP_DIR}" -name "minio_backup_*.tar.gz" \
  -mtime "+${RETENTION_DAYS}" -delete

# 统计文件数量
FILE_COUNT=$(mc ls --recursive "${SOURCE_ALIAS}/${SOURCE_BUCKET}" 2>/dev/null | wc -l)
log "当前 MinIO 文件总数：${FILE_COUNT}"

log "====== MinIO 备份完成 ======"
```

### 4.2 实时同步（持续监控）

```bash
#!/bin/bash
# 实时镜像同步（后台运行）
# 适用于关键课程资源的即时备份

mc mirror \
  --watch \
  --overwrite \
  coursebuddy/course-buddy \
  coursebuddy-backup/course-buddy-backup \
  2>&1 | tee /opt/backups/course-buddy/minio/watch.log &

echo "MinIO 实时同步已启动，PID: $!"
echo "日志：/opt/backups/course-buddy/minio/watch.log"
```

### 4.3 按类型选择性备份

```bash
#!/bin/bash
# 只备份关键数据（课程资源和作业提交）

mc cp --recursive \
  coursebuddy/course-buddy/courses/ \
  coursebuddy-backup/course-buddy-backup/courses/

mc cp --recursive \
  coursebuddy/course-buddy/submissions/ \
  coursebuddy-backup/course-buddy-backup/submissions/

echo "关键数据备份完成"
```

---

## 5. 存储生命周期管理

### 5.1 配置对象生命周期规则

```bash
# 创建生命周期配置文件
cat > /tmp/lifecycle.json << 'EOF'
{
  "Rules": [
    {
      "ID": "expire-temp-uploads",
      "Status": "Enabled",
      "Filter": {
        "Prefix": "temp/"
      },
      "Expiration": {
        "Days": 1
      }
    },
    {
      "ID": "expire-ocr-images",
      "Status": "Enabled",
      "Filter": {
        "Prefix": "ocr/images/"
      },
      "Expiration": {
        "Days": 90
      }
    },
    {
      "ID": "archive-old-submissions",
      "Status": "Enabled",
      "Filter": {
        "Prefix": "submissions/"
      },
      "Transition": {
        "Days": 180,
        "StorageClass": "STANDARD_IA"
      }
    }
  ]
}
EOF

# 应用生命周期规则
mc ilm import coursebuddy/course-buddy < /tmp/lifecycle.json

# 查看已配置的规则
mc ilm ls coursebuddy/course-buddy
```

### 5.2 生命周期规则说明

| 规则 | 前缀 | 策略 | 原因 |
|------|------|------|------|
| 清理临时上传 | `temp/` | 1 天后删除 | 分块上传失败留下的碎片 |
| 清理 OCR 图片 | `ocr/images/` | 90 天后删除 | OCR 识别后图片不再需要长期保留 |
| 归档旧提交 | `submissions/` | 180 天后转冷存储 | 降低存储成本 |
| 清理课程缩略图 | `courses/thumbs/` | 30 天后重新生成 | 避免旧缩略图占用空间 |

### 5.3 清理分块上传残留

```bash
# 查看未完成的分块上传（占用空间但不可访问）
mc ls --incomplete coursebuddy/course-buddy

# 清理超过 24 小时的未完成分块上传
mc rm --incomplete --recursive --force \
  --older-than 24h \
  coursebuddy/course-buddy

# 定期执行（建议每日凌晨运行）
# 添加到 crontab：
# 0 3 * * * mc rm --incomplete --recursive --force --older-than 24h coursebuddy/course-buddy
```

### 5.4 存储使用量分析

```bash
# 查看 Bucket 总大小
mc du coursebuddy/course-buddy

# 按前缀分析存储分布
echo "=== 存储分布分析 ==="
for prefix in courses notes submissions assignments ocr users knowledge; do
    SIZE=$(mc du "coursebuddy/course-buddy/${prefix}/" 2>/dev/null | tail -1 | awk '{print $1}')
    echo "  ${prefix}/: ${SIZE:-0}"
done
```

---

## 6. 灾难恢复

### 6.1 完整恢复流程

```bash
#!/bin/bash
# MinIO 灾难恢复脚本
# 从备份 MinIO 实例恢复数据

BACKUP_ALIAS="coursebuddy-backup"
TARGET_ALIAS="coursebuddy"
BACKUP_BUCKET="course-buddy-backup"
TARGET_BUCKET="course-buddy"

echo "====== 开始 MinIO 灾难恢复 ======"
echo "源：${BACKUP_ALIAS}/${BACKUP_BUCKET}"
echo "目标：${TARGET_ALIAS}/${TARGET_BUCKET}"

# 步骤 1：确认目标 MinIO 可访问
if ! mc admin info "${TARGET_ALIAS}" > /dev/null 2>&1; then
    echo "❌ 目标 MinIO 不可访问，请先确认服务已启动"
    exit 1
fi

# 步骤 2：创建目标 Bucket（如果不存在）
mc mb "${TARGET_ALIAS}/${TARGET_BUCKET}" 2>/dev/null || true

# 步骤 3：从备份恢复
echo "开始数据恢复，这可能需要较长时间..."
mc mirror \
  --overwrite \
  "${BACKUP_ALIAS}/${BACKUP_BUCKET}" \
  "${TARGET_ALIAS}/${TARGET_BUCKET}"

# 步骤 4：验证文件数量
SOURCE_COUNT=$(mc ls --recursive "${BACKUP_ALIAS}/${BACKUP_BUCKET}" | wc -l)
TARGET_COUNT=$(mc ls --recursive "${TARGET_ALIAS}/${TARGET_BUCKET}" | wc -l)
echo "源文件数：${SOURCE_COUNT}，目标文件数：${TARGET_COUNT}"

if [ "${SOURCE_COUNT}" -eq "${TARGET_COUNT}" ]; then
    echo "✅ 恢复成功，文件数量一致"
else
    echo "⚠️  文件数量不一致，请手动核查"
fi

echo "====== MinIO 灾难恢复完成 ======"
```

### 6.2 从本地文件系统恢复

```bash
# 从 tar 压缩包恢复
BACKUP_FILE="/opt/backups/course-buddy/minio/minio_backup_20240115_020000.tar.gz"
RESTORE_DIR="/tmp/minio-restore"

mkdir -p "${RESTORE_DIR}"
tar -xzf "${BACKUP_FILE}" -C "${RESTORE_DIR}"

# 上传到 MinIO
mc cp --recursive "${RESTORE_DIR}/" coursebuddy/course-buddy/

echo "从本地备份恢复完成"
rm -rf "${RESTORE_DIR}"
```

### 6.3 部分文件恢复

```bash
# 只恢复特定课程的资源（误删单个目录）
COURSE_ID="123"
mc mirror \
  coursebuddy-backup/course-buddy-backup/courses/resources/${COURSE_ID}/ \
  coursebuddy/course-buddy/courses/resources/${COURSE_ID}/

echo "课程 ${COURSE_ID} 资源恢复完成"

# 恢复特定用户的笔记附件
USER_ID="42"
mc mirror \
  coursebuddy-backup/course-buddy-backup/notes/attachments/${USER_ID}/ \
  coursebuddy/course-buddy/notes/attachments/${USER_ID}/

echo "用户 ${USER_ID} 笔记附件恢复完成"
```

### 6.4 Docker 数据卷级别恢复

```bash
# 当 MinIO 容器完全损坏时，从数据卷备份恢复

# 步骤 1：停止旧容器
docker-compose stop

# 步骤 2：删除损坏的卷（如果有）
# docker volume rm course-buddy-backend_minio_data  # 谨慎操作！

# 步骤 3：启动 MinIO（重新创建空卷）
docker-compose up -d minio

# 步骤 4：等待 MinIO 就绪后执行 mc mirror 恢复
sleep 15
mc alias set coursebuddy http://localhost:9000 minioadmin minioadmin
mc mb coursebuddy/course-buddy
mc mirror coursebuddy-backup/course-buddy-backup coursebuddy/course-buddy
```

---

## 7. MinIO 数据完整性验证

### 7.1 使用 mc stat 验证文件

```bash
# 检查特定文件是否存在且完整
mc stat coursebuddy/course-buddy/courses/resources/123/lecture1.pdf

# 输出示例：
# Name      : lecture1.pdf
# Date      : 2024-01-15 10:30:45 CST
# Size      : 2.5 MiB
# ETag      : d41d8cd98f00b204e9800998ecf8427e
# ContentType: application/pdf
```

### 7.2 批量校验脚本

```bash
#!/bin/bash
# 验证备份与源数据的 ETag（MD5）一致性
echo "开始数据完整性校验..."

# 生成源文件清单
mc ls --recursive --json coursebuddy/course-buddy/courses/ \
  | jq -r '.key + " " + .etag' \
  > /tmp/source_etags.txt

# 生成备份文件清单
mc ls --recursive --json coursebuddy-backup/course-buddy-backup/courses/ \
  | jq -r '.key + " " + .etag' \
  > /tmp/backup_etags.txt

# 对比差异
diff /tmp/source_etags.txt /tmp/backup_etags.txt && \
  echo "✅ 校验通过，源数据与备份完全一致" || \
  echo "⚠️  发现差异，请检查输出"

rm -f /tmp/source_etags.txt /tmp/backup_etags.txt
```

---

## 8. 监控与告警

### 8.1 存储容量监控

```bash
#!/bin/bash
# 检查 MinIO 存储使用情况

BUCKET_SIZE=$(mc du coursebuddy/course-buddy 2>/dev/null | tail -1 | awk '{print $1}')
echo "MinIO 存储总量：${BUCKET_SIZE}"

# 检查磁盘使用率（MinIO 数据目录）
MINIO_DATA_DIR="/var/lib/docker/volumes/course-buddy-backend_minio_data"
if [ -d "${MINIO_DATA_DIR}" ]; then
    USAGE=$(df "${MINIO_DATA_DIR}" | awk 'NR==2 {print $5}' | tr -d '%')
    if [ "${USAGE}" -gt 85 ]; then
        echo "CRITICAL: MinIO 磁盘使用率 ${USAGE}%，请立即扩容！"
    elif [ "${USAGE}" -gt 70 ]; then
        echo "WARNING: MinIO 磁盘使用率 ${USAGE}%，请关注"
    else
        echo "OK: MinIO 磁盘使用率 ${USAGE}%"
    fi
fi
```

### 8.2 MinIO 服务健康检查

```bash
# MinIO 健康检查端点
curl -sf http://localhost:9000/minio/health/live && echo "MinIO 存活" || echo "MinIO 不可用"
curl -sf http://localhost:9000/minio/health/ready && echo "MinIO 就绪" || echo "MinIO 未就绪"

# 通过 mc 检查
mc admin info coursebuddy | grep -E "uptime|drives"
```

### 8.3 备份任务监控 Crontab 完整配置

```bash
# /etc/crontab 或用户 crontab
# MinIO 每日增量备份（凌晨 3:00）
0 3 * * * /opt/course-buddy/scripts/backup_minio.sh >> /opt/backups/course-buddy/minio/cron.log 2>&1

# 清理分块上传残留（每日凌晨 3:30）
30 3 * * * mc rm --incomplete --recursive --force --older-than 24h coursebuddy/course-buddy >> /opt/backups/course-buddy/minio/cleanup.log 2>&1

# 存储使用量检查（每小时）
0 * * * * /opt/course-buddy/scripts/check_minio_storage.sh

# 备份验证（每周一上午 9:00）
0 9 * * 1 /opt/course-buddy/scripts/verify_minio_backup.sh >> /opt/backups/course-buddy/minio/verify.log 2>&1
```

---

## mc 常用命令速查

```bash
# 文件操作
mc ls coursebuddy/course-buddy/              # 列出文件
mc cp file.pdf coursebuddy/course-buddy/     # 上传文件
mc rm coursebuddy/course-buddy/file.pdf      # 删除文件
mc stat coursebuddy/course-buddy/file.pdf    # 查看文件信息
mc cat coursebuddy/course-buddy/config.json  # 查看文件内容

# 目录操作
mc mb coursebuddy/new-bucket                 # 创建 Bucket
mc rb coursebuddy/old-bucket --force         # 删除 Bucket（含数据）
mc du coursebuddy/course-buddy               # 统计大小

# 同步操作
mc mirror src/ coursebuddy/course-buddy/     # 本地到 MinIO
mc mirror coursebuddy/course-buddy/ dst/     # MinIO 到本地
mc mirror coursebuddy/a coursebuddy2/b       # MinIO 到 MinIO

# 管理操作
mc admin info coursebuddy                    # 服务信息
mc admin user list coursebuddy               # 用户列表
mc admin policy list coursebuddy             # 策略列表
```

---

## 参考资料

- [MinIO 官方文档](https://min.io/docs/minio/linux/index.html)
- [mc Client 完整命令参考](https://min.io/docs/minio/linux/reference/minio-mc.html)
- [MinIO 对象生命周期管理](https://min.io/docs/minio/linux/administration/object-management/object-lifecycle-management.html)
- [MinIO 站点复制（高可用）](https://min.io/docs/minio/linux/operations/install-deploy-manage/multi-site-replication.html)
