-- ============================================
-- FIX ALL USERS - 2M MARKET
-- ============================================
-- This script fixes both admin and employee passwords
-- Run this in phpMyAdmin SQL tab
-- ============================================

USE 2market;

-- Fix ADMIN user password
-- Password: admin123
UPDATE utilisateurs 
SET password_hash = '$2a$10$Xu3nfFWNbDsB2jLtomPEu.OzF0PjZ7yTSX5e3e7FwA39.UEXv9SV.' 
WHERE username = 'admin';

-- Fix EMPLOYEE user password (create if doesn't exist)
-- Password: admin123
INSERT INTO utilisateurs (username, password_hash, role) 
VALUES ('employe', '$2a$10$Xu3nfFWNbDsB2jLtomPEu.OzF0PjZ7yTSX5e3e7FwA39.UEXv9SV.', 'Employé')
ON DUPLICATE KEY UPDATE 
    password_hash = '$2a$10$Xu3nfFWNbDsB2jLtomPEu.OzF0PjZ7yTSX5e3e7FwA39.UEXv9SV.',
    role = 'Employé';

-- Verify both users
SELECT 
    id,
    username,
    role,
    LEFT(password_hash, 30) as hash_preview,
    CASE 
        WHEN password_hash LIKE '$2a$10$%' THEN '✓ Valid BCrypt'
        ELSE '✗ Invalid'
    END as status
FROM utilisateurs
WHERE username IN ('admin', 'employe')
ORDER BY username;

-- ============================================
-- LOGIN CREDENTIALS:
-- ============================================
-- 
-- Username: admin
-- Password: admin123
--
-- Username: employe  
-- Password: admin123
--
-- ============================================


