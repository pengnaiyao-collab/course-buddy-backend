-- V18：修复外键指向旧的 courses 表（应为 course_catalog）

-- 1. 修复 course_enrollments
ALTER TABLE course_enrollments DROP FOREIGN KEY fk_enrollment_course;
ALTER TABLE course_enrollments ADD CONSTRAINT fk_enrollment_course_catalog FOREIGN KEY (course_id) REFERENCES course_catalog (id) ON DELETE CASCADE;

-- 2. 修复 learning_progress
ALTER TABLE learning_progress DROP FOREIGN KEY fk_progress_course;
ALTER TABLE learning_progress ADD CONSTRAINT fk_progress_course_catalog FOREIGN KEY (course_id) REFERENCES course_catalog (id) ON DELETE CASCADE;

-- 3. 修复 course_discussions
ALTER TABLE course_discussions DROP FOREIGN KEY fk_discussion_course;
ALTER TABLE course_discussions ADD CONSTRAINT fk_discussion_course_catalog FOREIGN KEY (course_id) REFERENCES course_catalog (id) ON DELETE CASCADE;

-- 4. 修复 knowledge_items（该约束在 V3 中）
ALTER TABLE knowledge_items DROP FOREIGN KEY fk_knowledge_items_course;
ALTER TABLE knowledge_items ADD CONSTRAINT fk_knowledge_items_course_catalog FOREIGN KEY (course_id) REFERENCES course_catalog (id) ON DELETE CASCADE;

-- 5. 修复 teams
ALTER TABLE teams ADD CONSTRAINT fk_teams_course_catalog FOREIGN KEY (course_id) REFERENCES course_catalog (id) ON DELETE SET NULL;

-- 6. 修复其他引用 courses 的表（补充缺失约束）
ALTER TABLE collaboration_projects ADD CONSTRAINT fk_projects_course_catalog FOREIGN KEY (course_id) REFERENCES course_catalog (id) ON DELETE CASCADE;
ALTER TABLE notes ADD CONSTRAINT fk_notes_course_catalog FOREIGN KEY (course_id) REFERENCES course_catalog (id) ON DELETE CASCADE;
ALTER TABLE questions ADD CONSTRAINT fk_questions_course_catalog FOREIGN KEY (course_id) REFERENCES course_catalog (id) ON DELETE CASCADE;

-- 7. 为 V12 表补充缺失外键，保证数据完整性
ALTER TABLE lessons ADD CONSTRAINT fk_lessons_course_catalog FOREIGN KEY (course_id) REFERENCES course_catalog (id) ON DELETE CASCADE;
ALTER TABLE assignments ADD CONSTRAINT fk_assignments_course_catalog FOREIGN KEY (course_id) REFERENCES course_catalog (id) ON DELETE CASCADE;
ALTER TABLE grade_sheets ADD CONSTRAINT fk_grade_sheets_course_catalog FOREIGN KEY (course_id) REFERENCES course_catalog (id) ON DELETE CASCADE;
ALTER TABLE course_resources ADD CONSTRAINT fk_course_resources_course_catalog FOREIGN KEY (course_id) REFERENCES course_catalog (id) ON DELETE CASCADE;
ALTER TABLE attendances ADD CONSTRAINT fk_attendances_course_catalog FOREIGN KEY (course_id) REFERENCES course_catalog (id) ON DELETE CASCADE;
