-- ============================================
-- CREATE USERS - 2M MARKET (URGENT FIX)
-- ============================================
-- Run this script in phpMyAdmin to create users with correct passwords
-- ============================================

USE 2market;

-- Ensure table exists
CREATE TABLE IF NOT EXISTS utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Employé') NOT NULL DEFAULT 'Employé',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Delete existing users to recreate them
DELETE FROM utilisateurs WHERE username IN ('admin', 'employe');

-- Insert ADMIN user
-- Password: admin123
-- This hash is generated using BCrypt for password "admin123"
INSERT INTO utilisateurs (username, password_hash, role) VALUES
('admin', '$2a$10$Xu3nfFWNbDsB2jLtomPEu.OzF0PjZ7yTSX5e3e7FwA39.UEXv9SV.', 'Admin');

-- Insert EMPLOYEE user  
-- Password: admin123
-- Same hash as admin (both use password "admin123")
INSERT INTO utilisateurs (username, password_hash, role) VALUES
('employe', '$2a$10$Xu3nfFWNbDsB2jLtomPEu.OzF0PjZ7yTSX5e3e7FwA39.UEXv9SV.', 'Employé');

-- Verify the users
SELECT 
    id,
    username,
    role,
    CASE 
        WHEN password_hash LIKE '$2a$10$%' THEN '✓ Valid BCrypt hash'
        ELSE '✗ Invalid hash format'
    END as hash_status,
    LEFT(password_hash, 30) as hash_preview,
    date_creation
FROM utilisateurs
WHERE username IN ('admin', 'employe')
ORDER BY username;

-- ============================================
-- LOGIN INFORMATION:
-- ============================================
-- 
-- ADMIN USER:
--   Username: admin
--   Password: admin123
--   Role: Admin
--
-- EMPLOYEE USER:
--   Username: employe
--   Password: admin123
--   Role: Employé
--
-- ============================================
-- 
-- After running this script:
-- 1. Close and restart your Java application
-- 2. Try logging in with the credentials above
-- 3. If it still doesn't work, check that MySQL is running
--    and the database '2market' exists
--
-- ============================================


