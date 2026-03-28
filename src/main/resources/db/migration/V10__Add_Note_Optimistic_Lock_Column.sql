-- V10: Add optimistic lock version column to notes table
ALTER TABLE notes
    ADD COLUMN IF NOT EXISTS opt_lock_version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号';
