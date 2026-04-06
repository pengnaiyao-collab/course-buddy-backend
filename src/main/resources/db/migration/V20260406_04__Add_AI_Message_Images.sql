ALTER TABLE ai_conversation_messages
    ADD COLUMN image_data LONGTEXT NULL,
    ADD COLUMN image_mime_type VARCHAR(64) NULL,
    ADD COLUMN image_name VARCHAR(255) NULL;
