-- 删除 notes 的 description 与 tags 列（如存在）
SET @db := DATABASE();

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema=@db AND table_name='notes' AND column_name='description') = 1,
    'ALTER TABLE notes DROP COLUMN description', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema=@db AND table_name='notes' AND column_name='tags') = 1,
    'ALTER TABLE notes DROP COLUMN tags', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
