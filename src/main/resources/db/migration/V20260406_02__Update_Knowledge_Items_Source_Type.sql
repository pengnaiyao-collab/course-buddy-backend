-- V20260406_02：从 source_type 注释中移除 OCR
SET @db := DATABASE();

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='knowledge_items' AND column_name='source_type') = 1,
    'ALTER TABLE knowledge_items MODIFY COLUMN source_type VARCHAR(64) DEFAULT ''MANUAL'' COMMENT ''MANUAL, FILE, WEB（手动/文件/网页）''',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
