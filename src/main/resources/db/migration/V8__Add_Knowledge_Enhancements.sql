-- V8: Add knowledge base enhancements: associations, OCR, web imports, audit log entity

-- Knowledge item associations (links between knowledge items)
CREATE TABLE IF NOT EXISTS knowledge_associations (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    source_id       BIGINT      NOT NULL COMMENT '源知识点ID',
    target_id       BIGINT      NOT NULL COMMENT '目标知识点ID',
    relation_type   VARCHAR(64) NOT NULL DEFAULT 'RELATED' COMMENT '关联类型: RELATED, DERIVED_FROM, SUPPLEMENTS, CONFLICTS_WITH',
    description     VARCHAR(500),
    created_by      BIGINT,
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_knowledge_assoc (source_id, target_id),
    KEY idx_ka_source (source_id),
    KEY idx_ka_target (target_id),
    CONSTRAINT fk_ka_source FOREIGN KEY (source_id) REFERENCES knowledge_items (id) ON DELETE CASCADE,
    CONSTRAINT fk_ka_target FOREIGN KEY (target_id) REFERENCES knowledge_items (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识点关联表';

-- Web page imports
CREATE TABLE IF NOT EXISTS web_imports (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    course_id       BIGINT        NOT NULL,
    url             VARCHAR(2048) NOT NULL COMMENT '原始URL',
    title           VARCHAR(512),
    content         LONGTEXT      COMMENT '解析后的正文内容',
    html_content    LONGTEXT      COMMENT '原始HTML内容',
    status          VARCHAR(32)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, COMPLETED, FAILED',
    error_message   VARCHAR(1000),
    knowledge_item_id BIGINT      COMMENT '关联的知识点ID',
    created_by      BIGINT,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_web_imports_course (course_id),
    KEY idx_web_imports_status (status),
    CONSTRAINT fk_web_imports_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网页导入记录表';

-- OCR results
CREATE TABLE IF NOT EXISTS ocr_results (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    file_upload_id  BIGINT      COMMENT '关联的文件上传记录',
    object_name     VARCHAR(500) NOT NULL COMMENT 'MinIO对象名',
    extracted_text  LONGTEXT    COMMENT 'OCR识别文字',
    confidence      DOUBLE      COMMENT '识别置信度',
    language        VARCHAR(16) DEFAULT 'chi_sim+eng' COMMENT '识别语言',
    status          VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, COMPLETED, FAILED',
    error_message   VARCHAR(1000),
    created_by      BIGINT,
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_ocr_file_upload (file_upload_id),
    KEY idx_ocr_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OCR识别结果表';

-- Add extracted_text and status columns to knowledge_items for full-text search
ALTER TABLE knowledge_items
    ADD COLUMN IF NOT EXISTS extracted_text LONGTEXT COMMENT '从文件提取的文本内容',
    ADD COLUMN IF NOT EXISTS source_type    VARCHAR(64) DEFAULT 'MANUAL' COMMENT 'MANUAL, FILE, WEB, OCR',
    ADD COLUMN IF NOT EXISTS status        VARCHAR(32) DEFAULT 'PUBLISHED' COMMENT 'DRAFT, PENDING_REVIEW, PUBLISHED, REJECTED';

-- Full-text index on knowledge_items for search
ALTER TABLE knowledge_items ADD FULLTEXT INDEX IF NOT EXISTS ft_knowledge_items_search (title, description, tags);
