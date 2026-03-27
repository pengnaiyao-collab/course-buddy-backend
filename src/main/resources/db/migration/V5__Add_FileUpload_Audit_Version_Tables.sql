-- V5__Add_FileUpload_Audit_Version_Tables.sql

-- File upload records
CREATE TABLE IF NOT EXISTS file_uploads (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    object_name VARCHAR(500) NOT NULL,
    file_name   VARCHAR(255) NOT NULL,
    file_size   BIGINT       NOT NULL,
    content_type VARCHAR(100),
    upload_url  VARCHAR(1000),
    uploaded_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    uploaded_by BIGINT,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_uploads_object_name (object_name(255)),
    INDEX idx_file_uploads_uploaded_by (uploaded_by),
    INDEX idx_file_uploads_uploaded_at (uploaded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Version records
CREATE TABLE IF NOT EXISTS versions (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    entity_type    VARCHAR(100) NOT NULL,
    entity_id      BIGINT       NOT NULL,
    version_number INT          NOT NULL,
    content        LONGTEXT     NOT NULL,
    created_by     BIGINT,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description    VARCHAR(500),
    PRIMARY KEY (id),
    UNIQUE KEY uk_versions_entity_version (entity_type, entity_id, version_number),
    INDEX idx_versions_entity (entity_type, entity_id),
    INDEX idx_versions_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Content review records
CREATE TABLE IF NOT EXISTS content_reviews (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    content_type VARCHAR(100) NOT NULL,
    content_id   BIGINT       NOT NULL,
    reviewer_id  BIGINT       NOT NULL,
    status       VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    comments     VARCHAR(500),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at  TIMESTAMP    NULL,
    PRIMARY KEY (id),
    INDEX idx_content_reviews_status (status),
    INDEX idx_content_reviews_content (content_type, content_id),
    INDEX idx_content_reviews_reviewer (reviewer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Audit logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    entity_type   VARCHAR(100) NOT NULL,
    entity_id     BIGINT,
    action        VARCHAR(50)  NOT NULL,
    old_value     LONGTEXT,
    new_value     LONGTEXT,
    operator_id   BIGINT,
    operator_name VARCHAR(100),
    ip_address    VARCHAR(50),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_audit_logs_entity (entity_type, entity_id),
    INDEX idx_audit_logs_operator (operator_id),
    INDEX idx_audit_logs_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
