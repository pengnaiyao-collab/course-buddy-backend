-- 为 users 表新增 student_number 列
ALTER TABLE users ADD COLUMN student_number VARCHAR(32);

-- 为 student_number 添加唯一约束以防重复
ALTER TABLE users ADD UNIQUE KEY uk_users_student_number (student_number);
