-- V2__seed_admin_user.sql
-- Seed initial admin user
-- Password: Admin@123 (BCrypt encoded)

INSERT INTO users (username, email, password, full_name, role, enabled, created_at, updated_at)
VALUES (
    'admin',
    'admin@coursebuddy.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'System Administrator',
    'ADMIN',
    1,
    NOW(),
    NOW()
);
