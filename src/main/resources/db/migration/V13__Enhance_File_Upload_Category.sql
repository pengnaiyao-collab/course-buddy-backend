-- V13__Enhance_File_Upload_Category.sql

-- Add category support for upload records so frontend classification
-- can be stored and retrieved reliably.
ALTER TABLE file_uploads
    ADD COLUMN category VARCHAR(64) NULL AFTER content_type;

CREATE INDEX idx_file_uploads_category ON file_uploads(category);
