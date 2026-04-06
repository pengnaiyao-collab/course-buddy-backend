-- V4：新增认证字段与 tokens 表
-- 为 users 表新增认证相关字段并创建 tokens 表

ALTER TABLE users
    ADD COLUMN real_name     VARCHAR(50),
    ADD COLUMN phone         VARCHAR(20),
    ADD COLUMN avatar        LONGTEXT,
    ADD COLUMN is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN is_locked     BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN last_login_at TIMESTAMP NULL;

CREATE TABLE IF NOT EXISTS tokens (
    id                 BIGINT    NOT NULL AUTO_INCREMENT,
    user_id            BIGINT    NOT NULL,
    access_token       LONGTEXT  NOT NULL,
    refresh_token      LONGTEXT  NOT NULL,
    expires_at         TIMESTAMP NOT NULL,
    refresh_expires_at TIMESTAMP NOT NULL,
    is_revoked         BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tokens_refresh_token (refresh_token(255)),
    KEY idx_token_user_id (user_id),
    CONSTRAINT fk_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
