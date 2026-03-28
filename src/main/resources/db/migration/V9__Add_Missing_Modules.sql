-- V9: Add missing modules: course enrollment, learning progress, discussions,
--     notifications, teams, note categories, note sharing, user profile extensions

-- ============================================================
-- 1. User profile extensions
-- ============================================================
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS bio          TEXT           COMMENT '个人简介',
    ADD COLUMN IF NOT EXISTS avatar_url   VARCHAR(512)   COMMENT '头像URL';

-- ============================================================
-- 2. User settings
-- ============================================================
CREATE TABLE IF NOT EXISTS user_settings (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    user_id             BIGINT       NOT NULL UNIQUE,
    notify_email        TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '邮件通知开关',
    notify_push         TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '推送通知开关',
    privacy_profile     VARCHAR(16)  NOT NULL DEFAULT 'PUBLIC' COMMENT 'PUBLIC/FRIENDS/PRIVATE',
    language            VARCHAR(8)   NOT NULL DEFAULT 'zh_CN',
    theme               VARCHAR(16)  NOT NULL DEFAULT 'light',
    timezone            VARCHAR(64)  NOT NULL DEFAULT 'Asia/Shanghai',
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_settings_user (user_id),
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户设置表';

-- ============================================================
-- 3. Course enrollments
-- ============================================================
CREATE TABLE IF NOT EXISTS course_enrollments (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    course_id       BIGINT      NOT NULL,
    user_id         BIGINT      NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, DROPPED, COMPLETED',
    enrolled_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dropped_at      DATETIME,
    completed_at    DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_enrollment (course_id, user_id),
    KEY idx_enrollment_user (user_id),
    KEY idx_enrollment_course (course_id),
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_user   FOREIGN KEY (user_id)   REFERENCES users   (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选课记录表';

-- ============================================================
-- 4. Learning progress
-- ============================================================
CREATE TABLE IF NOT EXISTS learning_progress (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    user_id         BIGINT      NOT NULL,
    course_id       BIGINT      NOT NULL,
    resource_id     BIGINT      COMMENT '资源ID（可选，某具体资源的进度）',
    progress        INT         NOT NULL DEFAULT 0 COMMENT '进度百分比 0-100',
    study_minutes   INT         NOT NULL DEFAULT 0 COMMENT '累计学习时长（分钟）',
    last_studied_at DATETIME    COMMENT '最近学习时间',
    notes           VARCHAR(512),
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_progress (user_id, course_id),
    KEY idx_progress_user   (user_id),
    KEY idx_progress_course (course_id),
    CONSTRAINT fk_progress_user   FOREIGN KEY (user_id)   REFERENCES users   (id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习进度表';

-- ============================================================
-- 5. Course discussions
-- ============================================================
CREATE TABLE IF NOT EXISTS course_discussions (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    course_id       BIGINT       NOT NULL,
    parent_id       BIGINT       COMMENT '父讨论ID（为null时为顶层讨论）',
    author_id       BIGINT       NOT NULL,
    title           VARCHAR(256) COMMENT '标题（仅顶层讨论有标题）',
    content         TEXT         NOT NULL,
    like_count      INT          NOT NULL DEFAULT 0,
    is_pinned       TINYINT(1)   NOT NULL DEFAULT 0,
    is_deleted      TINYINT(1)   NOT NULL DEFAULT 0,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_discussion_course  (course_id),
    KEY idx_discussion_parent  (parent_id),
    KEY idx_discussion_author  (author_id),
    CONSTRAINT fk_discussion_course  FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT fk_discussion_author  FOREIGN KEY (author_id) REFERENCES users   (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程讨论表';

-- Discussion likes
CREATE TABLE IF NOT EXISTS discussion_likes (
    id              BIGINT   NOT NULL AUTO_INCREMENT,
    discussion_id   BIGINT   NOT NULL,
    user_id         BIGINT   NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_discussion_like (discussion_id, user_id),
    KEY idx_dlike_discussion (discussion_id),
    KEY idx_dlike_user       (user_id),
    CONSTRAINT fk_dlike_discussion FOREIGN KEY (discussion_id) REFERENCES course_discussions (id) ON DELETE CASCADE,
    CONSTRAINT fk_dlike_user       FOREIGN KEY (user_id)       REFERENCES users             (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='讨论点赞表';

-- ============================================================
-- 6. Notifications
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    title           VARCHAR(256) NOT NULL,
    content         TEXT,
    type            VARCHAR(32)  NOT NULL DEFAULT 'SYSTEM' COMMENT 'SYSTEM, COURSE, TASK, MENTION, LIKE',
    is_read         TINYINT(1)   NOT NULL DEFAULT 0,
    related_id      BIGINT       COMMENT '关联对象ID',
    related_type    VARCHAR(32)  COMMENT '关联对象类型',
    expires_at      DATETIME,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_notification_user    (user_id),
    KEY idx_notification_is_read (is_read),
    KEY idx_notification_type    (type),
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- ============================================================
-- 7. Messages (private chat)
-- ============================================================
CREATE TABLE IF NOT EXISTS messages (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    sender_id       BIGINT       NOT NULL,
    receiver_id     BIGINT       NOT NULL,
    content         TEXT         NOT NULL,
    msg_type        VARCHAR(16)  NOT NULL DEFAULT 'TEXT' COMMENT 'TEXT, IMAGE, FILE',
    is_read         TINYINT(1)   NOT NULL DEFAULT 0,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_msg_sender   (sender_id),
    KEY idx_msg_receiver (receiver_id),
    KEY idx_msg_is_read  (is_read),
    CONSTRAINT fk_msg_sender   FOREIGN KEY (sender_id)   REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_receiver FOREIGN KEY (receiver_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信消息表';

-- ============================================================
-- 8. Teams
-- ============================================================
CREATE TABLE IF NOT EXISTS teams (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    name            VARCHAR(128) NOT NULL,
    description     TEXT,
    avatar_url      VARCHAR(512),
    owner_id        BIGINT       NOT NULL,
    course_id       BIGINT,
    project_id      BIGINT,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_team_owner   (owner_id),
    KEY idx_team_course  (course_id),
    CONSTRAINT fk_team_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队表';

-- Team members
CREATE TABLE IF NOT EXISTS team_members (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    team_id         BIGINT      NOT NULL,
    user_id         BIGINT      NOT NULL,
    role            VARCHAR(16) NOT NULL DEFAULT 'MEMBER' COMMENT 'ADMIN, MANAGER, MEMBER',
    joined_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_team_member (team_id, user_id),
    KEY idx_tm_team (team_id),
    KEY idx_tm_user (user_id),
    CONSTRAINT fk_tm_team FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE,
    CONSTRAINT fk_tm_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队成员表';

-- ============================================================
-- 9. Note categories
-- ============================================================
CREATE TABLE IF NOT EXISTS note_categories (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    name            VARCHAR(64)  NOT NULL,
    color           VARCHAR(16)  COMMENT '分类颜色（hex）',
    sort_order      INT          NOT NULL DEFAULT 0,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_note_cat_user (user_id),
    CONSTRAINT fk_note_cat_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记分类表';

-- ============================================================
-- 10. Note tags
-- ============================================================
CREATE TABLE IF NOT EXISTS note_tags (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    user_id         BIGINT      NOT NULL,
    name            VARCHAR(64) NOT NULL,
    color           VARCHAR(16) COMMENT '标签颜色',
    use_count       INT         NOT NULL DEFAULT 0,
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_note_tag (user_id, name),
    KEY idx_note_tag_user (user_id),
    CONSTRAINT fk_note_tag_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记标签表';

-- ============================================================
-- 11. Note shares
-- ============================================================
CREATE TABLE IF NOT EXISTS note_shares (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    note_id         BIGINT       NOT NULL,
    owner_id        BIGINT       NOT NULL,
    share_token     VARCHAR(64)  NOT NULL UNIQUE COMMENT '分享token',
    permission      VARCHAR(16)  NOT NULL DEFAULT 'READ' COMMENT 'READ, EDIT',
    expires_at      DATETIME,
    access_count    INT          NOT NULL DEFAULT 0,
    is_active       TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_note_share_note  (note_id),
    KEY idx_note_share_owner (owner_id),
    KEY idx_note_share_token (share_token),
    CONSTRAINT fk_note_share_note  FOREIGN KEY (note_id)  REFERENCES notes (id) ON DELETE CASCADE,
    CONSTRAINT fk_note_share_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记分享表';

-- ============================================================
-- 12. Note versions
-- ============================================================
CREATE TABLE IF NOT EXISTS note_versions (
    id              BIGINT   NOT NULL AUTO_INCREMENT,
    note_id         BIGINT   NOT NULL,
    version_no      INT      NOT NULL,
    title           VARCHAR(256) NOT NULL,
    content         LONGTEXT NOT NULL,
    changed_by      BIGINT   NOT NULL,
    change_desc     VARCHAR(500),
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_nv_note    (note_id),
    KEY idx_nv_version (version_no),
    CONSTRAINT fk_nv_note    FOREIGN KEY (note_id)    REFERENCES notes (id) ON DELETE CASCADE,
    CONSTRAINT fk_nv_changed FOREIGN KEY (changed_by) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记版本历史表';
