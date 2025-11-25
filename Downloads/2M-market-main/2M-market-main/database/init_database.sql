-- Script d'initialisation de la base de données pour 2M Market
-- Créer la base de données
CREATE DATABASE IF NOT EXISTS 2market CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE 2market;

-- Table des utilisateurs
CREATE TABLE IF NOT EXISTS utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    passwordHash VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Employé') NOT NULL DEFAULT 'Employé',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table des produits
CREATE TABLE IF NOT EXISTS produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codeBarre VARCHAR(50) NOT NULL UNIQUE,
    nom VARCHAR(255) NOT NULL,
    prixAchatActuel DECIMAL(10, 2) NOT NULL,
    prixVenteDefaut DECIMAL(10, 2) NOT NULL,
    quantiteStock INT NOT NULL DEFAULT 0,
    seuilAlerte INT NOT NULL DEFAULT 10,
    INDEX idx_codeBarre (codeBarre),
    INDEX idx_nom (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table des ventes
CREATE TABLE IF NOT EXISTS ventes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    dateVente DATETIME NOT NULL,
    totalVente DECIMAL(10, 2) NOT NULL,
    utilisateurId INT NOT NULL,
    FOREIGN KEY (utilisateurId) REFERENCES utilisateurs(id) ON DELETE RESTRICT,
    INDEX idx_dateVente (dateVente),
    INDEX idx_utilisateurId (utilisateurId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table des détails de vente
CREATE TABLE IF NOT EXISTS detailventes (
    id_detail INT AUTO_INCREMENT PRIMARY KEY,
    venteId INT NOT NULL,
    produitId INT NOT NULL,
    quantite INT NOT NULL,
    prixVenteUnitaire DECIMAL(10, 2) NOT NULL,
    prixAchatUnitaire DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (venteId) REFERENCES ventes(id) ON DELETE CASCADE,
    FOREIGN KEY (produitId) REFERENCES produits(id) ON DELETE RESTRICT,
    INDEX idx_venteId (venteId),
    INDEX idx_produitId (produitId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertion d'utilisateurs par défaut
-- 
-- IMPORTANT: Les hachages BCrypt sont générés de manière unique à chaque fois.
-- Les utilisateurs par défaut seront créés automatiquement par l'application
-- lors du premier démarrage, ou vous pouvez exécuter:
--   mvn compile exec:java -Dexec.mainClass="util.DatabaseSetup"
--
-- Comptes par défaut qui seront créés:
--   - admin / admin123 (Admin)
--   - employe / admin123 (Employé)
--
-- Si vous voulez créer les utilisateurs manuellement via SQL, vous devez d'abord
-- générer les hachages BCrypt en exécutant PasswordHashGenerator.

-- NOTE: Si les hachages ci-dessus ne fonctionnent pas, générez-en de nouveaux
-- en exécutant PasswordHashGenerator ou créez les utilisateurs via l'interface admin.

-- Insertion de quelques produits d'exemple
INSERT INTO produits (codeBarre, nom, prixAchatActuel, prixVenteDefaut, quantiteStock, seuilAlerte) VALUES
('1234567890123', 'Paquet de Pâtes', 0.50, 1.20, 150, 20),
('1234567890124', 'Riz 1kg', 1.00, 2.50, 80, 15),
('1234567890125', 'Huile d\'olive 1L', 3.50, 6.00, 45, 10),
('1234567890126', 'Sucre 1kg', 0.80, 1.80, 120, 25),
('1234567890127', 'Café 250g', 2.50, 5.00, 60, 12),
('1234567890128', 'Thé 100g', 1.20, 3.00, 90, 15),
('1234567890129', 'Lait 1L', 0.60, 1.50, 200, 30),
('1234567890130', 'Pain de mie', 0.40, 1.00, 100, 20)
ON DUPLICATE KEY UPDATE codeBarre=codeBarre;

