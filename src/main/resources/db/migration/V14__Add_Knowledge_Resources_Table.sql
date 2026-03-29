-- V14__Add_Knowledge_Resources_Table.sql

CREATE TABLE IF NOT EXISTS knowledge_resources (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    knowledge_item_id BIGINT       NOT NULL,
    resource_type     VARCHAR(32)  NOT NULL COMMENT 'NOTE, SLIDE, EXAM, HOMEWORK, VIDEO, AUDIO, LINK',
    title             VARCHAR(256) NOT NULL,
    url               VARCHAR(512) NOT NULL,
    description       VARCHAR(500),
    created_by        BIGINT,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_kr_item (knowledge_item_id),
    KEY idx_kr_type (resource_type),
    CONSTRAINT fk_kr_item FOREIGN KEY (knowledge_item_id) REFERENCES knowledge_items (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
