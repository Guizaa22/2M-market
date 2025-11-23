-- Script pour ajouter une table de catégories et mettre à jour la structure

USE 2market;

-- Créer la table des catégories
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_nom (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insérer des catégories par défaut
INSERT INTO categories (nom, description) VALUES
('Alimentaire', 'Produits alimentaires de base'),
('Boissons', 'Boissons et liquides'),
('Tabac', 'Produits de tabac'),
('Hygiène', 'Produits d''hygiène et de soins'),
('Divers', 'Autres produits')
ON DUPLICATE KEY UPDATE nom=nom;

-- Ajouter une colonne category_id à la table produits si elle n'existe pas
-- Note: Si la colonne categorie existe déjà, on va la migrer
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = '2market' 
    AND TABLE_NAME = 'produits' 
    AND COLUMN_NAME = 'category_id');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE produits ADD COLUMN category_id INT NULL AFTER categorie, ADD FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL',
    'SELECT "Column category_id already exists" AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Migrer les données existantes de categorie (VARCHAR) vers category_id (INT)
-- Cette requête associe les catégories existantes aux nouvelles catégories
UPDATE produits p
LEFT JOIN categories c ON LOWER(TRIM(p.categorie)) = LOWER(TRIM(c.nom))
SET p.category_id = c.id
WHERE p.categorie IS NOT NULL AND p.categorie != '';

-- Afficher les statistiques
SELECT 
    'Migration terminée' AS status,
    (SELECT COUNT(*) FROM produits WHERE category_id IS NOT NULL) AS produits_avec_categorie,
    (SELECT COUNT(*) FROM categories) AS nombre_categories;

