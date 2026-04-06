-- V13：上传分类增强

-- 为上传记录增加分类支持，方便前端分类
-- 可被可靠存储与读取。
ALTER TABLE file_uploads
    ADD COLUMN category VARCHAR(64) NULL AFTER content_type;

CREATE INDEX idx_file_uploads_category ON file_uploads(category);
