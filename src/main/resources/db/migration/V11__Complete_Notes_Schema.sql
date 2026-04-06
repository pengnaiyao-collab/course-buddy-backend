-- V11：完善笔记模块结构
-- 为 notes 与 note_categories 补充缺失列，并创建 note_tag_relations 表

-- ============================================================
-- 仅在缺失时为 notes 添加列（兼容 MySQL 5.7）
SET @db := DATABASE();

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='notes' AND column_name='category_id') = 0,
    'ALTER TABLE notes ADD COLUMN category_id BIGINT COMMENT ''所属分类ID（FK到note_categories）''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='notes' AND column_name='description') = 0,
    'ALTER TABLE notes ADD COLUMN description TEXT COMMENT ''笔记摘要/ 描述''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='notes' AND column_name='status') = 0,
    'ALTER TABLE notes ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT ''DRAFT'' COMMENT ''DRAFT/PUBLISHED/ARCHIVED''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='notes' AND column_name='is_public') = 0,
    'ALTER TABLE notes ADD COLUMN is_public TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否公开分享''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='notes' AND column_name='is_deleted') = 0,
    'ALTER TABLE notes ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否软删除''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='notes' AND column_name='deleted_at') = 0,
    'ALTER TABLE notes ADD COLUMN deleted_at DATETIME COMMENT ''软删除时间''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db AND table_name='note_categories' AND column_name='description') = 0,
    'ALTER TABLE note_categories ADD COLUMN description TEXT COMMENT ''分类描述''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 3. 创建 note_tag_relations 表
-- ============================================================
CREATE TABLE IF NOT EXISTS note_tag_relations (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    note_id    BIGINT   NOT NULL,
    tag_id     BIGINT   NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_note_tag_relation (note_id, tag_id),
    KEY idx_ntr_note (note_id),
    KEY idx_ntr_tag  (tag_id),
    CONSTRAINT fk_ntr_note FOREIGN KEY (note_id) REFERENCES notes     (id) ON DELETE CASCADE,
    CONSTRAINT fk_ntr_tag  FOREIGN KEY (tag_id)  REFERENCES note_tags (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记-标签关联表';
