-- Script pour ajouter la colonne unité aux produits
-- Supporte: kg, grammes, unité (par défaut), litre, etc.

USE 2market;

-- Ajouter la colonne unite si elle n'existe pas
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = '2market' 
    AND TABLE_NAME = 'produits' 
    AND COLUMN_NAME = 'unite');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE produits ADD COLUMN unite VARCHAR(20) DEFAULT "unité" AFTER quantite_stock',
    'SELECT "Column unite already exists" AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Mettre à jour les produits existants avec une unité par défaut
UPDATE produits SET unite = 'unité' WHERE unite IS NULL;

-- Afficher le résultat
SELECT 'Colonne unite ajoutée avec succès' AS status;

