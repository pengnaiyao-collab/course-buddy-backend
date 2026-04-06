-- V10：为 notes 表新增乐观锁版本列
ALTER TABLE notes
    ADD COLUMN opt_lock_version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号';
