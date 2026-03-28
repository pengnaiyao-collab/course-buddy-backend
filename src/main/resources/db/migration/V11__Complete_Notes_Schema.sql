-- V11: Complete Notes module schema
-- Adds missing columns to notes and note_categories, and creates note_tag_relations table

-- ============================================================
-- 1. Add missing columns to notes table
-- ============================================================
ALTER TABLE notes
    ADD COLUMN IF NOT EXISTS category_id BIGINT      COMMENT '所属分类ID（FK到note_categories）',
    ADD COLUMN IF NOT EXISTS description TEXT        COMMENT '笔记摘要/描述',
    ADD COLUMN IF NOT EXISTS status      VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/ARCHIVED',
    ADD COLUMN IF NOT EXISTS is_public   TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否公开分享',
    ADD COLUMN IF NOT EXISTS is_deleted  TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否软删除',
    ADD COLUMN IF NOT EXISTS deleted_at  DATETIME    COMMENT '软删除时间';

-- ============================================================
-- 2. Add description column to note_categories
-- ============================================================
ALTER TABLE note_categories
    ADD COLUMN IF NOT EXISTS description TEXT COMMENT '分类描述';

-- ============================================================
-- 3. Create note_tag_relations table
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
