-- V2：初始化管理员用户
-- 初始化管理员用户
-- 密码：Admin@123（BCrypt 加密后的正确哈希）

INSERT INTO users (username, email, password, full_name, role, enabled, created_at, updated_at)
VALUES (
    'admin',
    'admin@coursebuddy.com',
    '$2a$10$uFdbNeg47/0L97psSAV8N.WV9r//tl2tCDNLMfdLcFUfPq5BtlKTS',
    'System Administrator',
    'ADMIN',
    1,
    NOW(),
    NOW()
);
