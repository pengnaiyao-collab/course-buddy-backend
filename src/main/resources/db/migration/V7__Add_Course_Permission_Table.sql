-- V7：新增课程权限等级（L1-L4）

CREATE TABLE IF NOT EXISTS course_permissions (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    user_id          BIGINT      NOT NULL,
    course_id        BIGINT      NOT NULL,
    permission_level VARCHAR(2)  NOT NULL COMMENT 'L1=管理员, L2=核心协作, L3=班级成员, L4=访客',
    granted_by       BIGINT,
    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_course (user_id, course_id),
    KEY idx_course_perm_course (course_id),
    KEY idx_course_perm_user (user_id),
    KEY idx_course_perm_level (permission_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程权限表';
