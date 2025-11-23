-- Script pour mettre à jour le mot de passe de l'utilisateur admin
-- Le hash BCrypt pour "admin123" a été généré
-- Exécutez ce script dans phpMyAdmin

UPDATE utilisateurs 
SET password_hash = '$2a$10$Xu3nfFWNbDsB2jLtomPEu.OzF0PjZ7yTSX5e3e7FwA39.UEXv9SV.' 
WHERE username = 'admin';

-- Le mot de passe est maintenant: admin123
-- Après avoir exécuté ce script, vous pourrez vous connecter avec:
-- Username: admin
-- Password: admin123

