-- V15：治理权限与双重审核

CREATE TABLE IF NOT EXISTS course_action_permissions (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    course_id         BIGINT       NOT NULL,
    permission_level  VARCHAR(2)   NOT NULL,
    action_key        VARCHAR(32)  NOT NULL,
    allowed           TINYINT(1)   NOT NULL DEFAULT 0,
    updated_by        BIGINT,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_action_level (course_id, permission_level, action_key),
    KEY idx_cap_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS course_admin_votes (
    id                 BIGINT      NOT NULL AUTO_INCREMENT,
    course_id          BIGINT      NOT NULL,
    candidate_user_id  BIGINT      NOT NULL,
    voter_user_id      BIGINT      NOT NULL,
    created_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_candidate_voter (course_id, candidate_user_id, voter_user_id),
    KEY idx_cav_course (course_id),
    KEY idx_cav_candidate (candidate_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE content_reviews
    ADD COLUMN second_reviewer_id BIGINT NULL,
    ADD COLUMN required_approvals INT NOT NULL DEFAULT 2,
    ADD COLUMN approval_count INT NOT NULL DEFAULT 0,
    ADD COLUMN moderation_status VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    ADD COLUMN violation_reason VARCHAR(500) NULL;

CREATE TABLE IF NOT EXISTS content_review_decisions (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    review_id    BIGINT      NOT NULL,
    reviewer_id  BIGINT      NOT NULL,
    decision     VARCHAR(16) NOT NULL,
    comments     VARCHAR(500),
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_review_reviewer (review_id, reviewer_id),
    KEY idx_crd_review (review_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
