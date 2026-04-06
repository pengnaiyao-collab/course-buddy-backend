-- V6：新增讯飞星火 AI 相关表
-- 讯飞星火 AI 对接相关数据表

-- AI 对话会话表
CREATE TABLE IF NOT EXISTS ai_conversations (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    title      VARCHAR(255),
    model      VARCHAR(64)  NOT NULL DEFAULT 'generalv3.5',
    status     VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME     NOT NULL,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY idx_ai_conversations_user (user_id),
    KEY idx_ai_conversations_status (status),
    CONSTRAINT fk_ai_conversations_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AI 对话消息表
CREATE TABLE IF NOT EXISTS ai_conversation_messages (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    conversation_id BIGINT      NOT NULL,
    role            VARCHAR(16) NOT NULL COMMENT 'user | assistant | system',
    content         TEXT        NOT NULL,
    token_count     INT,
    created_at      DATETIME    NOT NULL,
    PRIMARY KEY (id),
    KEY idx_ai_messages_conversation (conversation_id),
    CONSTRAINT fk_ai_messages_conversation FOREIGN KEY (conversation_id)
        REFERENCES ai_conversations (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AI 智能生成内容表
CREATE TABLE IF NOT EXISTS ai_generated_contents (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL,
    content_type VARCHAR(32)  NOT NULL COMMENT 'OUTLINE | EXAM_POINTS | QUESTIONS | BREAKDOWN',
    subject      VARCHAR(128),
    course_id    BIGINT,
    prompt       TEXT,
    content      LONGTEXT,
    status       VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING | COMPLETED | FAILED',
    token_count  INT,
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME,
    PRIMARY KEY (id),
    KEY idx_ai_generated_user (user_id),
    KEY idx_ai_generated_type (content_type),
    KEY idx_ai_generated_status (status),
    CONSTRAINT fk_ai_generated_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AI 使用统计表
CREATE TABLE IF NOT EXISTS ai_usage_stats (
    id                 BIGINT      NOT NULL AUTO_INCREMENT,
    user_id            BIGINT,
    model              VARCHAR(64),
    request_type       VARCHAR(64) COMMENT 'CHAT | CHAT_STREAM | OUTLINE | EXAM_POINTS | QUESTIONS | BREAKDOWN',
    prompt_tokens      INT,
    completion_tokens  INT,
    total_tokens       INT,
    duration_ms        BIGINT,
    status             VARCHAR(16) COMMENT 'SUCCESS | FAILED',
    error_message      TEXT,
    created_at         DATETIME    NOT NULL,
    PRIMARY KEY (id),
    KEY idx_ai_usage_user (user_id),
    KEY idx_ai_usage_type (request_type),
    KEY idx_ai_usage_status (status),
    KEY idx_ai_usage_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AI 错误日志表
CREATE TABLE IF NOT EXISTS ai_error_logs (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT,
    request_type VARCHAR(64),
    error_code   VARCHAR(32),
    error_message TEXT,
    request_body TEXT,
    created_at   DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_ai_error_user (user_id),
    KEY idx_ai_error_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
