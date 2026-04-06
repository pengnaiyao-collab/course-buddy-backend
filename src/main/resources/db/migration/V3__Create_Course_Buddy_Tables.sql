-- V3：创建课程/知识库/AI/协作/笔记相关表
-- 创建知识库、AI 助手、协作与笔记模块相关表

-- 知识库
CREATE TABLE IF NOT EXISTS knowledge_items (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    course_id    BIGINT       NOT NULL,
    title        VARCHAR(256) NOT NULL,
    description  TEXT,
    file_url     VARCHAR(512),
    file_type    VARCHAR(64),
    category     VARCHAR(64),
    tags         VARCHAR(256),
    created_by   BIGINT,
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME,
    PRIMARY KEY (id),
    KEY idx_knowledge_items_course (course_id),
    KEY idx_knowledge_items_category (category),
    CONSTRAINT fk_knowledge_items_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT fk_knowledge_items_creator FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AI 助手：问题
CREATE TABLE IF NOT EXISTS questions (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    course_id  BIGINT,
    user_id    BIGINT   NOT NULL,
    content    TEXT     NOT NULL,
    subject    VARCHAR(64),
    status     VARCHAR(16) NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY idx_questions_user (user_id),
    KEY idx_questions_course (course_id),
    CONSTRAINT fk_questions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AI 助手：答案
CREATE TABLE IF NOT EXISTS answers (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    question_id BIGINT      NOT NULL,
    content     TEXT        NOT NULL,
    source      VARCHAR(32) NOT NULL DEFAULT 'AI',
    created_at  DATETIME    NOT NULL,
    updated_at  DATETIME,
    PRIMARY KEY (id),
    KEY idx_answers_question (question_id),
    CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AI 助手：学习任务
CREATE TABLE IF NOT EXISTS learning_tasks (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    course_id   BIGINT,
    title       VARCHAR(256) NOT NULL,
    description TEXT,
    status      VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    due_date    DATE,
    priority    VARCHAR(16) NOT NULL DEFAULT 'MEDIUM',
    created_at  DATETIME    NOT NULL,
    updated_at  DATETIME,
    PRIMARY KEY (id),
    KEY idx_learning_tasks_user (user_id),
    KEY idx_learning_tasks_status (status),
    CONSTRAINT fk_learning_tasks_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 协作：项目
CREATE TABLE IF NOT EXISTS collaboration_projects (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    course_id   BIGINT,
    name        VARCHAR(256) NOT NULL,
    description TEXT,
    owner_id    BIGINT       NOT NULL,
    status      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME,
    PRIMARY KEY (id),
    KEY idx_collab_projects_owner (owner_id),
    KEY idx_collab_projects_course (course_id),
    CONSTRAINT fk_collab_projects_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 协作：任务
CREATE TABLE IF NOT EXISTS collaboration_tasks (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    project_id  BIGINT       NOT NULL,
    title       VARCHAR(256) NOT NULL,
    description TEXT,
    assignee_id BIGINT,
    status      VARCHAR(16)  NOT NULL DEFAULT 'TODO',
    priority    VARCHAR(16)  NOT NULL DEFAULT 'MEDIUM',
    due_date    DATE,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME,
    PRIMARY KEY (id),
    KEY idx_collab_tasks_project (project_id),
    KEY idx_collab_tasks_assignee (assignee_id),
    CONSTRAINT fk_collab_tasks_project FOREIGN KEY (project_id) REFERENCES collaboration_projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_collab_tasks_assignee FOREIGN KEY (assignee_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 笔记
CREATE TABLE IF NOT EXISTS notes (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    course_id  BIGINT,
    title      VARCHAR(256) NOT NULL,
    content    TEXT         NOT NULL,
    category   VARCHAR(64),
    tags       VARCHAR(256),
    created_at DATETIME     NOT NULL,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY idx_notes_user (user_id),
    KEY idx_notes_course (course_id),
    CONSTRAINT fk_notes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
