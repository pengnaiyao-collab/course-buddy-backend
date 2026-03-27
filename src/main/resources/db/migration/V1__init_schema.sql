-- V1__init_schema.sql
-- Initial database schema for Course Buddy

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    username    VARCHAR(64)     NOT NULL,
    email       VARCHAR(128)    NOT NULL,
    password    VARCHAR(256)    NOT NULL,
    full_name   VARCHAR(64),
    role        VARCHAR(16)     NOT NULL DEFAULT 'STUDENT',
    enabled     TINYINT(1)      NOT NULL DEFAULT 1,
    created_at  DATETIME        NOT NULL,
    updated_at  DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS courses (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    title           VARCHAR(128)    NOT NULL,
    description     TEXT,
    teacher_id      BIGINT,
    price           DECIMAL(10,2),
    category        VARCHAR(64),
    status          VARCHAR(16)     NOT NULL DEFAULT 'DRAFT',
    max_students    INT,
    cover_image_url VARCHAR(512),
    created_at      DATETIME        NOT NULL,
    updated_at      DATETIME,
    PRIMARY KEY (id),
    KEY idx_courses_teacher (teacher_id),
    KEY idx_courses_status (status),
    KEY idx_courses_category (category),
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
