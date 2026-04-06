-- 为教师审核流程在 users 表中新增 status 列
ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';

-- 将现有 ADMIN/STUDENT 设为 ACTIVE，待审核教师设为 PENDING
UPDATE users SET status = 'ACTIVE' WHERE role IN ('ADMIN', 'STUDENT');
UPDATE users SET status = 'PENDING' WHERE role = 'TEACHER' AND status IS NULL;
