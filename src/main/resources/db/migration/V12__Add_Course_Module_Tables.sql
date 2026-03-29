CREATE TABLE IF NOT EXISTS course_catalog (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(32) UNIQUE,
    name VARCHAR(256) NOT NULL,
    description TEXT,
    instructor_id BIGINT NOT NULL,
    department_id BIGINT,
    credit_hours INT NOT NULL DEFAULT 3,
    level VARCHAR(16) DEFAULT 'BEGINNER',
    capacity INT NOT NULL DEFAULT 30,
    enrolled_count INT NOT NULL DEFAULT 0,
    thumbnail_url VARCHAR(512),
    syllabus TEXT,
    max_grade INT NOT NULL DEFAULT 100,
    passing_grade INT NOT NULL DEFAULT 60,
    status VARCHAR(16) DEFAULT 'OPEN',
    start_date DATE,
    end_date DATE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS lessons (
    id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    title VARCHAR(256) NOT NULL,
    description TEXT,
    content TEXT,
    lesson_order INT NOT NULL DEFAULT 1,
    duration INT DEFAULT 0,
    video_url VARCHAR(512),
    resource_urls TEXT,
    is_published TINYINT(1) DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS assignments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    title VARCHAR(256) NOT NULL,
    description TEXT,
    due_date DATETIME,
    max_score INT DEFAULT 100,
    attachment_url VARCHAR(512),
    is_published TINYINT(1) DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS assignment_submissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    assignment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    submission_url TEXT,
    submitted_at DATETIME,
    score INT,
    feedback TEXT,
    graded_at DATETIME,
    graded_by BIGINT,
    status VARCHAR(16) DEFAULT 'SUBMITTED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS grade_sheets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    assignment_score INT DEFAULT 0,
    participation_score INT DEFAULT 0,
    quiz_score INT DEFAULT 0,
    midterm_score INT,
    final_score INT,
    total_score INT DEFAULT 0,
    grade VARCHAR(4),
    grade_date DATETIME,
    comments TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_grade_sheet_course_student (course_id, student_id)
);

CREATE TABLE IF NOT EXISTS course_resources (
    id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    title VARCHAR(256) NOT NULL,
    description TEXT,
    resource_type VARCHAR(16) DEFAULT 'OTHER',
    resource_url TEXT NOT NULL,
    file_size BIGINT,
    download_count INT DEFAULT 0,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS attendances (
    id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    session_date DATE NOT NULL,
    status VARCHAR(16) DEFAULT 'PRESENT',
    remarks VARCHAR(512),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
