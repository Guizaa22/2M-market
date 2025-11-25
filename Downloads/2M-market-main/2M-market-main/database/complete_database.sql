-- ============================================
-- 2M MARKET - COMPLETE DATABASE FOR PHP/MySQL
-- ============================================
-- Complete database schema with executable SQL queries
-- Can be run directly in phpMyAdmin or MySQL command line
-- Database: 2market
-- ============================================

-- Create database
CREATE DATABASE IF NOT EXISTS 2market CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE 2market;

-- ============================================
-- TABLE STRUCTURES
-- ============================================

-- Table: utilisateurs
DROP TABLE IF EXISTS detailsvente;
DROP TABLE IF EXISTS ventes;
DROP TABLE IF EXISTS produits;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS utilisateurs;

CREATE TABLE utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Employé') NOT NULL DEFAULT 'Employé',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: categories
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_nom (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: produits
CREATE TABLE produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code_barre VARCHAR(50) NOT NULL UNIQUE,
    nom VARCHAR(255) NOT NULL,
    categorie VARCHAR(50),
    category_id INT NULL,
    prix_achat_actuel DECIMAL(10, 2) NOT NULL,
    prix_vente_defaut DECIMAL(10, 2) NOT NULL,
    quantite_stock INT NOT NULL DEFAULT 0,
    unite VARCHAR(20) DEFAULT 'unité',
    seuil_alerte INT NOT NULL DEFAULT 10,
    date_derniere_maj TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code_barre (code_barre),
    INDEX idx_nom (nom),
    INDEX idx_category_id (category_id),
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: ventes
CREATE TABLE ventes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date_vente DATETIME NOT NULL,
    total_vente DECIMAL(10, 2) NOT NULL,
    id_utilisateur INT NOT NULL,
    type_paiement ENUM('Espèces', 'Carte', 'Autre') DEFAULT 'Espèces',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_date_vente (date_vente),
    INDEX idx_id_utilisateur (id_utilisateur),
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateurs(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: detailsvente
CREATE TABLE detailsvente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_vente INT NOT NULL,
    id_produit INT NOT NULL,
    quantite INT NOT NULL,
    prix_vente_unitaire DECIMAL(10, 2) NOT NULL,
    prix_achat_unitaire DECIMAL(10, 2) NOT NULL,
    INDEX idx_id_vente (id_vente),
    INDEX idx_id_produit (id_produit),
    FOREIGN KEY (id_vente) REFERENCES ventes(id) ON DELETE CASCADE,
    FOREIGN KEY (id_produit) REFERENCES produits(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- SAMPLE DATA
-- ============================================

-- Insert default users (admin and employee)
-- Password for both: admin123
INSERT INTO utilisateurs (username, password_hash, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin'),
('employe', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Employé')
ON DUPLICATE KEY UPDATE 
    password_hash = VALUES(password_hash),
    role = VALUES(role);

-- Insert default categories
INSERT INTO categories (nom, description) VALUES
('Alimentaire', 'Produits alimentaires de base'),
('Boissons', 'Boissons et liquides'),
('Tabac', 'Produits de tabac'),
('Hygiène', 'Produits d''hygiène et de soins'),
('Divers', 'Autres produits')
ON DUPLICATE KEY UPDATE nom=nom;

-- Insert sample products
INSERT INTO produits (code_barre, nom, categorie, category_id, prix_achat_actuel, prix_vente_defaut, quantite_stock, unite, seuil_alerte) VALUES
('1234567890123', 'Paquet de Pâtes', 'Alimentaire', 1, 0.50, 1.20, 150, 'unité', 20),
('1234567890124', 'Riz 1kg', 'Alimentaire', 1, 1.00, 2.50, 80, 'kg', 15),
('1234567890125', 'Huile d''olive 1L', 'Alimentaire', 1, 3.50, 6.00, 45, 'L', 10),
('1234567890126', 'Sucre 1kg', 'Alimentaire', 1, 0.80, 1.80, 120, 'kg', 25),
('1234567890127', 'Café 250g', 'Alimentaire', 1, 2.50, 5.00, 60, 'unité', 12),
('1234567890128', 'Thé 100g', 'Alimentaire', 1, 1.20, 3.00, 90, 'unité', 15),
('1234567890129', 'Lait 1L', 'Boissons', 2, 0.60, 1.50, 200, 'L', 30),
('1234567890130', 'Pain de mie', 'Alimentaire', 1, 0.40, 1.00, 100, 'unité', 20)
ON DUPLICATE KEY UPDATE code_barre=code_barre;

-- ============================================
-- USEFUL SQL QUERIES (EXECUTABLE)
-- ============================================

-- View all tables
SHOW TABLES;

-- ============================================
-- UTILISATEURS QUERIES
-- ============================================

-- Get all users
SELECT * FROM utilisateurs ORDER BY username;

-- Get user by ID (replace 1 with actual ID)
SELECT * FROM utilisateurs WHERE id = 1;

-- Get user by username (replace 'admin' with actual username)
SELECT * FROM utilisateurs WHERE username = 'admin';

-- Check if username exists
SELECT COUNT(*) as exists FROM utilisateurs WHERE username = 'admin';

-- Get all admins
SELECT * FROM utilisateurs WHERE role = 'Admin';

-- Get all employees
SELECT * FROM utilisateurs WHERE role = 'Employé';

-- ============================================
-- CATEGORIES QUERIES
-- ============================================

-- Get all categories
SELECT * FROM categories ORDER BY nom;

-- Get category by ID
SELECT * FROM categories WHERE id = 1;

-- Get category by name
SELECT * FROM categories WHERE nom = 'Alimentaire';

-- Count products per category
SELECT c.nom, COUNT(p.id) as nombre_produits
FROM categories c
LEFT JOIN produits p ON c.id = p.category_id
GROUP BY c.id, c.nom
ORDER BY nombre_produits DESC;

-- ============================================
-- PRODUITS QUERIES
-- ============================================

-- Get all products
SELECT * FROM produits ORDER BY nom;

-- Get product by ID
SELECT * FROM produits WHERE id = 1;

-- Get product by barcode
SELECT * FROM produits WHERE code_barre = '1234567890123';

-- Get product by name
SELECT * FROM produits WHERE nom = 'Paquet de Pâtes';

-- Search products by name (partial match)
SELECT * FROM produits WHERE nom LIKE '%Pâtes%' ORDER BY nom;

-- Search products by barcode (partial match)
SELECT * FROM produits WHERE code_barre LIKE '%123%' ORDER BY nom;

-- Get products with low stock
SELECT * FROM produits WHERE quantite_stock <= seuil_alerte ORDER BY quantite_stock ASC;

-- Get products by category name
SELECT p.* 
FROM produits p 
INNER JOIN categories c ON p.category_id = c.id 
WHERE c.nom = 'Alimentaire' AND p.quantite_stock > 0 
ORDER BY p.nom;

-- Get products by category ID
SELECT * FROM produits WHERE category_id = 1 AND quantite_stock > 0 ORDER BY nom;

-- Get all distinct categories from products (legacy)
SELECT DISTINCT categorie FROM produits WHERE categorie IS NOT NULL AND categorie != '' ORDER BY categorie;

-- Get total inventory value
SELECT 
    COUNT(*) AS nombre_produits,
    SUM(quantite_stock) AS total_quantite,
    SUM(quantite_stock * prix_achat_actuel) AS valeur_stock_achat,
    SUM(quantite_stock * prix_vente_defaut) AS valeur_stock_vente,
    SUM(quantite_stock * (prix_vente_defaut - prix_achat_actuel)) AS profit_potentiel
FROM produits;

-- Check if barcode exists
SELECT COUNT(*) as exists FROM produits WHERE code_barre = '1234567890123';

-- ============================================
-- VENTES QUERIES
-- ============================================

-- Get all sales
SELECT * FROM ventes ORDER BY date_vente DESC;

-- Get recent sales (last 10)
SELECT * FROM ventes ORDER BY date_vente DESC LIMIT 10;

-- Get sales by user ID
SELECT * FROM ventes WHERE id_utilisateur = 1 ORDER BY date_vente DESC;

-- Get sales with user information
SELECT 
    v.id,
    v.date_vente,
    v.total_vente,
    u.username AS vendeur,
    u.role,
    COUNT(dv.id) AS nombre_produits
FROM ventes v
INNER JOIN utilisateurs u ON v.id_utilisateur = u.id
LEFT JOIN detailsvente dv ON v.id = dv.id_vente
GROUP BY v.id, v.date_vente, v.total_vente, u.username, u.role
ORDER BY v.date_vente DESC;

-- Get sales for date range (replace dates)
SELECT * FROM ventes 
WHERE date_vente BETWEEN '2024-01-01 00:00:00' AND '2024-12-31 23:59:59'
ORDER BY date_vente DESC;

-- Get daily sales summary
SELECT 
    DATE(date_vente) AS date_vente,
    COUNT(id) AS nombre_ventes,
    SUM(total_vente) AS total_ventes,
    AVG(total_vente) AS moyenne_vente
FROM ventes
GROUP BY DATE(date_vente)
ORDER BY date_vente DESC
LIMIT 30;

-- Get monthly sales summary
SELECT 
    YEAR(date_vente) AS annee,
    MONTH(date_vente) AS mois,
    DATE_FORMAT(date_vente, '%Y-%m') AS periode,
    COUNT(id) AS nombre_ventes,
    SUM(total_vente) AS total_ventes,
    AVG(total_vente) AS moyenne_vente
FROM ventes
GROUP BY YEAR(date_vente), MONTH(date_vente)
ORDER BY annee DESC, mois DESC;

-- Get total revenue for period
SELECT SUM(total_vente) AS total_revenue
FROM ventes 
WHERE date_vente BETWEEN '2024-01-01 00:00:00' AND '2024-12-31 23:59:59';

-- Get number of sales for period
SELECT COUNT(*) AS nombre_ventes
FROM ventes 
WHERE date_vente BETWEEN '2024-01-01 00:00:00' AND '2024-12-31 23:59:59';

-- ============================================
-- DETAILS VENTE QUERIES
-- ============================================

-- Get sale details by sale ID
SELECT * FROM detailsvente WHERE id_vente = 1;

-- Get complete sale details with product info
SELECT 
    dv.id,
    dv.id_vente,
    p.nom AS produit,
    p.code_barre,
    dv.quantite,
    dv.prix_vente_unitaire,
    dv.prix_achat_unitaire,
    (dv.quantite * dv.prix_vente_unitaire) AS total_ligne,
    (dv.quantite * (dv.prix_vente_unitaire - dv.prix_achat_unitaire)) AS profit_ligne
FROM detailsvente dv
INNER JOIN produits p ON dv.id_produit = p.id
WHERE dv.id_vente = 1
ORDER BY dv.id;

-- Get number of articles in a sale
SELECT COALESCE(SUM(quantite), 0) AS nombre_articles
FROM detailsvente 
WHERE id_vente = 1;

-- ============================================
-- STATISTICS QUERIES
-- ============================================

-- Get top selling products
SELECT 
    p.id,
    p.nom,
    p.code_barre,
    SUM(dv.quantite) AS quantite_vendue,
    SUM(dv.quantite * dv.prix_vente_unitaire) AS chiffre_affaires,
    COUNT(DISTINCT dv.id_vente) AS nombre_ventes
FROM detailsvente dv
INNER JOIN produits p ON dv.id_produit = p.id
GROUP BY p.id, p.nom, p.code_barre
ORDER BY quantite_vendue DESC
LIMIT 20;

-- Get most profitable products
SELECT 
    p.id,
    p.nom,
    p.code_barre,
    SUM(dv.quantite) AS quantite_vendue,
    SUM(dv.quantite * (dv.prix_vente_unitaire - dv.prix_achat_unitaire)) AS profit_total,
    AVG(dv.prix_vente_unitaire - dv.prix_achat_unitaire) AS profit_unitaire_moyen
FROM detailsvente dv
INNER JOIN produits p ON dv.id_produit = p.id
GROUP BY p.id, p.nom, p.code_barre
ORDER BY profit_total DESC
LIMIT 20;

-- Get sales by category
SELECT 
    c.nom AS categorie,
    COUNT(DISTINCT dv.id_vente) AS nombre_ventes,
    SUM(dv.quantite) AS quantite_vendue,
    SUM(dv.quantite * dv.prix_vente_unitaire) AS chiffre_affaires,
    SUM(dv.quantite * (dv.prix_vente_unitaire - dv.prix_achat_unitaire)) AS profit
FROM categories c
INNER JOIN produits p ON c.id = p.category_id
INNER JOIN detailsvente dv ON p.id = dv.id_produit
GROUP BY c.id, c.nom
ORDER BY chiffre_affaires DESC;

-- Get total profit for period
SELECT 
    SUM((dv.prix_vente_unitaire - dv.prix_achat_unitaire) * dv.quantite) AS profit_total
FROM detailsvente dv
JOIN ventes v ON dv.id_vente = v.id
WHERE v.date_vente BETWEEN '2024-01-01 00:00:00' AND '2024-12-31 23:59:59';

-- Get overall financial summary
SELECT 
    COUNT(DISTINCT v.id) AS total_ventes,
    SUM(v.total_vente) AS chiffre_affaires_total,
    SUM(dv.quantite * dv.prix_achat_unitaire) AS cout_achat_total,
    SUM(dv.quantite * (dv.prix_vente_unitaire - dv.prix_achat_unitaire)) AS profit_brut_total,
    ROUND((SUM(dv.quantite * (dv.prix_vente_unitaire - dv.prix_achat_unitaire)) / SUM(v.total_vente)) * 100, 2) AS marge_pourcentage,
    AVG(v.total_vente) AS panier_moyen
FROM ventes v
INNER JOIN detailsvente dv ON v.id = dv.id_vente;

-- Get sales statistics by user
SELECT 
    u.id,
    u.username,
    u.role,
    COUNT(v.id) AS nombre_ventes,
    SUM(v.total_vente) AS total_ventes,
    AVG(v.total_vente) AS moyenne_vente,
    MIN(v.date_vente) AS premiere_vente,
    MAX(v.date_vente) AS derniere_vente
FROM utilisateurs u
LEFT JOIN ventes v ON u.id = v.id_utilisateur
GROUP BY u.id, u.username, u.role
ORDER BY total_ventes DESC;

-- ============================================
-- TOBACCO SPECIFIC QUERIES
-- ============================================

-- Get tobacco sales
SELECT DISTINCT v.* 
FROM ventes v
INNER JOIN detailsvente dv ON v.id = dv.id_vente
INNER JOIN produits p ON dv.id_produit = p.id
WHERE LOWER(p.categorie) LIKE '%tabac%' 
   OR LOWER(p.categorie) LIKE '%puff%' 
   OR LOWER(p.categorie) LIKE '%terrea%'
   OR LOWER(p.categorie) LIKE '%cigarette%'
ORDER BY v.date_vente DESC;

-- Get total tobacco sales for period
SELECT SUM(dv.prix_vente_unitaire * dv.quantite) AS total_tabac
FROM detailsvente dv
INNER JOIN ventes v ON dv.id_vente = v.id
INNER JOIN produits p ON dv.id_produit = p.id
WHERE v.date_vente BETWEEN '2024-01-01 00:00:00' AND '2024-12-31 23:59:59'
  AND (LOWER(p.categorie) LIKE '%tabac%' 
   OR LOWER(p.categorie) LIKE '%puff%' 
   OR LOWER(p.categorie) LIKE '%terrea%'
   OR LOWER(p.categorie) LIKE '%cigarette%');

-- Get top tobacco products
SELECT 
    p.nom, 
    SUM(dv.quantite) AS quantite_totale, 
    SUM(dv.prix_vente_unitaire * dv.quantite) AS ca_total
FROM detailsvente dv
INNER JOIN ventes v ON dv.id_vente = v.id
INNER JOIN produits p ON dv.id_produit = p.id
WHERE LOWER(p.categorie) LIKE '%tabac%' 
   OR LOWER(p.categorie) LIKE '%puff%' 
   OR LOWER(p.categorie) LIKE '%terrea%'
   OR LOWER(p.categorie) LIKE '%cigarette%'
GROUP BY p.id, p.nom
ORDER BY quantite_totale DESC
LIMIT 10;

-- ============================================
-- MAINTENANCE QUERIES
-- ============================================

-- Check database integrity
SELECT 'Ventes sans utilisateur' AS probleme, COUNT(*) AS nombre
FROM ventes v
LEFT JOIN utilisateurs u ON v.id_utilisateur = u.id
WHERE u.id IS NULL

UNION ALL

SELECT 'Details de vente sans vente' AS probleme, COUNT(*) AS nombre
FROM detailsvente dv
LEFT JOIN ventes v ON dv.id_vente = v.id
WHERE v.id IS NULL

UNION ALL

SELECT 'Details de vente sans produit' AS probleme, COUNT(*) AS nombre
FROM detailsvente dv
LEFT JOIN produits p ON dv.id_produit = p.id
WHERE p.id IS NULL

UNION ALL

SELECT 'Produits sans categorie' AS probleme, COUNT(*) AS nombre
FROM produits p
WHERE p.category_id IS NULL AND (p.categorie IS NULL OR p.categorie = '');

-- Get table sizes
SELECT 
    table_name AS table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb,
    table_rows AS nombre_lignes
FROM information_schema.TABLES
WHERE table_schema = '2market'
ORDER BY (data_length + index_length) DESC;

-- Get all table row counts
SELECT 'utilisateurs' AS table_name, COUNT(*) AS count FROM utilisateurs
UNION ALL
SELECT 'categories', COUNT(*) FROM categories
UNION ALL
SELECT 'produits', COUNT(*) FROM produits
UNION ALL
SELECT 'ventes', COUNT(*) FROM ventes
UNION ALL
SELECT 'detailsvente', COUNT(*) FROM detailsvente;

-- View foreign key relationships
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = '2market'
  AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME;

-- ============================================
-- DASHBOARD QUERIES
-- ============================================

-- Complete dashboard statistics
SELECT 
    (SELECT COUNT(*) FROM produits) AS total_produits,
    (SELECT COUNT(*) FROM utilisateurs) AS total_utilisateurs,
    (SELECT COUNT(*) FROM ventes) AS total_ventes,
    (SELECT SUM(total_vente) FROM ventes) AS chiffre_affaires_total,
    (SELECT COUNT(*) FROM produits WHERE quantite_stock <= seuil_alerte) AS produits_alerte,
    (SELECT SUM(quantite_stock) FROM produits) AS stock_total;

-- ============================================
-- END OF FILE
-- ============================================

