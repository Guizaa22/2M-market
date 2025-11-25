-- ============================================
-- FIX USERS PASSWORD - 2M MARKET
-- ============================================
-- This script will create/update admin and employee users
-- with correct BCrypt password hashes
-- Password for both users: admin123
-- ============================================

USE 2market;

-- Delete existing users if they exist (to recreate them properly)
DELETE FROM utilisateurs WHERE username IN ('admin', 'employe');

-- Create ADMIN user with BCrypt hash for password "admin123"
INSERT INTO utilisateurs (username, password_hash, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin');

-- Create EMPLOYEE user with BCrypt hash for password "admin123"
INSERT INTO utilisateurs (username, password_hash, role) VALUES
('employe', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Employé');

-- Verify users were created
SELECT 
    id,
    username,
    role,
    LEFT(password_hash, 30) as hash_preview,
    date_creation
FROM utilisateurs
WHERE username IN ('admin', 'employe')
ORDER BY username;

-- ============================================
-- LOGIN CREDENTIALS:
-- ============================================
-- 
-- ADMIN:
--   Username: admin
--   Password: admin123
--
-- EMPLOYEE:
--   Username: employe
--   Password: admin123
--
-- ============================================


