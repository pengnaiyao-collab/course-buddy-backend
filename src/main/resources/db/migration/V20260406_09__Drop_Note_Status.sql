-- 删除笔记状态列

SET @db := DATABASE();
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='notes' AND column_name='status') = 0,
    'SELECT 1',
    'ALTER TABLE notes DROP COLUMN status');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=@db AND table_name='notes' AND index_name='idx_notes_status') = 0,
    'SELECT 1',
    'ALTER TABLE notes DROP INDEX idx_notes_status');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
