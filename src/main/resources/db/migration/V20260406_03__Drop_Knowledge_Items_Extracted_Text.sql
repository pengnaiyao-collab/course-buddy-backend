-- V20260406_03：删除 knowledge_items 的 extracted_text
SET @db := DATABASE();

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='knowledge_items' AND column_name='extracted_text') = 1,
    'ALTER TABLE knowledge_items DROP COLUMN extracted_text',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
