-- V20240603：新增笔记附件字段
-- 为 notes 增加附件列（附件链接的 JSON 数组）

SET @db := DATABASE();

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema=@db AND table_name='notes' AND column_name='attachments') = 0,
    'ALTER TABLE notes ADD COLUMN attachments JSON COMMENT ''附件链接列表（JSON 数组）''',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
