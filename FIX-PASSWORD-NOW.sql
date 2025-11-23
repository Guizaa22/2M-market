-- ⚠️ IMPORTANT: Exécutez ce script dans phpMyAdmin MAINTENANT !
-- Ce script va corriger le mot de passe de l'utilisateur admin

USE 2market;

-- Mettre à jour le mot de passe avec un hash BCrypt valide pour "admin123"
UPDATE utilisateurs 
SET password_hash = '$2a$10$Xu3nfFWNbDsB2jLtomPEu.OzF0PjZ7yTSX5e3e7FwA39.UEXv9SV.' 
WHERE username = 'admin';

-- Vérification
SELECT id, username, LEFT(password_hash, 20) as hash_preview, role 
FROM utilisateurs 
WHERE username = 'admin';

-- Après avoir exécuté ce script, vous pourrez vous connecter avec:
-- Username: admin
-- Password: admin123

