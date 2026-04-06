-- V20240602：补充用户资料字段
-- 为 users 表补充缺失的资料字段并移除废弃的 email 列

ALTER TABLE users
    ADD COLUMN nickname      VARCHAR(64)  AFTER username,
    ADD COLUMN gender        VARCHAR(10)  COMMENT 'MALE, FEMALE, UNKNOWN' AFTER phone,
    ADD COLUMN school        VARCHAR(128) AFTER gender,
    ADD COLUMN last_login_ip VARCHAR(45)  AFTER last_login_at;

-- 按简化要求移除废弃的 email 列
-- 如存在先删除唯一约束
-- 注意：MySQL 删除唯一索引使用 DROP INDEX
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND INDEX_NAME = 'uk_users_email') > 0,
  'ALTER TABLE users DROP INDEX uk_users_email',
  'SELECT 1'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'email') > 0,
  'ALTER TABLE users DROP COLUMN email',
  'SELECT 1'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 如未正确处理，则将 full_name 重命名为 real_name
-- （V4 添加了 real_name，V1 中有 full_name，避免重复或混淆）
-- 实际上 V4 只添加了 real_name，未删除 full_name。
-- 如存在则清理 full_name 与 enabled。
SET @dbname = DATABASE();
SET @tablename = 'users';

-- 删除 full_name
SET @columnname = 'full_name';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'ALTER TABLE users DROP COLUMN full_name',
  'SELECT 1'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 删除 enabled
SET @columnname = 'enabled';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'ALTER TABLE users DROP COLUMN enabled',
  'SELECT 1'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
