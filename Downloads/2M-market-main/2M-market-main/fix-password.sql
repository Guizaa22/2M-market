-- Script pour corriger le mot de passe de l'utilisateur admin
-- Le mot de passe "admin123" sera hashé avec BCrypt
-- Exécutez ce script dans phpMyAdmin après avoir généré le hash

-- Pour générer le hash, exécutez:
-- mvn compile exec:java -Dexec.mainClass=util.PasswordHashGenerator -Dexec.args=admin123

-- Ensuite, remplacez le hash ci-dessous par celui généré et exécutez cette requête:

UPDATE utilisateurs 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE username = 'admin';

-- Le hash ci-dessus correspond au mot de passe "admin123"
-- Si vous voulez un nouveau hash, utilisez PasswordHashGenerator

