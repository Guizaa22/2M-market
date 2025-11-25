-- ============================================
-- 2M MARKET - USEFUL SQL QUERIES
-- ============================================
-- Collection of useful SQL queries for the 2M Market application
-- Database: 2market
-- ============================================

USE 2market;

-- ============================================
-- 1. PRODUCT QUERIES
-- ============================================

-- Get all products with their categories
SELECT 
    p.id,
    p.codeBarre,
    p.nom,
    c.nom AS categorie,
    p.prixAchatActuel,
    p.prixVenteDefaut,
    p.quantiteStock,
    p.seuilAlerte,
    p.unite,
    CASE 
        WHEN p.quantiteStock <= p.seuilAlerte THEN 'ALERTE'
        ELSE 'OK'
    END AS statut_stock
FROM produits p
LEFT JOIN categories c ON p.category_id = c.id
ORDER BY p.nom;

-- Get products with low stock (below alert threshold)
SELECT 
    p.id,
    p.codeBarre,
    p.nom,
    c.nom AS categorie,
    p.quantiteStock,
    p.seuilAlerte,
    (p.seuilAlerte - p.quantiteStock) AS quantite_manquante,
    p.unite
FROM produits p
LEFT JOIN categories c ON p.category_id = c.id
WHERE p.quantiteStock <= p.seuilAlerte
ORDER BY (p.seuilAlerte - p.quantiteStock) DESC;

-- Search products by name or barcode
SELECT 
    p.id,
    p.codeBarre,
    p.nom,
    c.nom AS categorie,
    p.prixVenteDefaut,
    p.quantiteStock,
    p.unite
FROM produits p
LEFT JOIN categories c ON p.category_id = c.id
WHERE p.nom LIKE '%SEARCH_TERM%' 
   OR p.codeBarre LIKE '%SEARCH_TERM%'
ORDER BY p.nom;

-- Get products by category
SELECT 
    p.id,
    p.codeBarre,
    p.nom,
    p.prixAchatActuel,
    p.prixVenteDefaut,
    p.quantiteStock,
    p.unite
FROM produits p
LEFT JOIN categories c ON p.category_id = c.id
WHERE c.nom = 'CATEGORY_NAME'
ORDER BY p.nom;

-- Get total value of inventory
SELECT 
    COUNT(*) AS nombre_produits,
    SUM(quantiteStock) AS total_quantite,
    SUM(quantiteStock * prixAchatActuel) AS valeur_stock_achat,
    SUM(quantiteStock * prixVenteDefaut) AS valeur_stock_vente,
    SUM(quantiteStock * (prixVenteDefaut - prixAchatActuel)) AS profit_potentiel
FROM produits;

-- ============================================
-- 2. SALES QUERIES
-- ============================================

-- Get all sales with user information
SELECT 
    v.id,
    v.dateVente,
    v.totalVente,
    u.username AS vendeur,
    u.role,
    COUNT(dv.id_detail) AS nombre_produits
FROM ventes v
INNER JOIN utilisateurs u ON v.utilisateurId = u.id
LEFT JOIN detailventes dv ON v.id = dv.venteId
GROUP BY v.id, v.dateVente, v.totalVente, u.username, u.role
ORDER BY v.dateVente DESC;

-- Get sales for a specific date range
SELECT 
    v.id,
    v.dateVente,
    v.totalVente,
    u.username AS vendeur,
    COUNT(dv.id_detail) AS nombre_produits
FROM ventes v
INNER JOIN utilisateurs u ON v.utilisateurId = u.id
LEFT JOIN detailventes dv ON v.id = dv.venteId
WHERE v.dateVente BETWEEN '2024-01-01' AND '2024-12-31'
GROUP BY v.id, v.dateVente, v.totalVente, u.username
ORDER BY v.dateVente DESC;

-- Get daily sales summary
SELECT 
    DATE(v.dateVente) AS date_vente,
    COUNT(v.id) AS nombre_ventes,
    SUM(v.totalVente) AS total_ventes,
    AVG(v.totalVente) AS moyenne_vente
FROM ventes v
GROUP BY DATE(v.dateVente)
ORDER BY date_vente DESC
LIMIT 30;

-- Get monthly sales summary
SELECT 
    YEAR(v.dateVente) AS annee,
    MONTH(v.dateVente) AS mois,
    DATE_FORMAT(v.dateVente, '%Y-%m') AS periode,
    COUNT(v.id) AS nombre_ventes,
    SUM(v.totalVente) AS total_ventes,
    AVG(v.totalVente) AS moyenne_vente
FROM ventes v
GROUP BY YEAR(v.dateVente), MONTH(v.dateVente)
ORDER BY annee DESC, mois DESC;

-- Get sales details for a specific sale
SELECT 
    dv.id_detail,
    p.nom AS produit,
    p.codeBarre,
    dv.quantite,
    dv.prixVenteUnitaire,
    dv.prixAchatUnitaire,
    (dv.quantite * dv.prixVenteUnitaire) AS total_ligne,
    (dv.quantite * (dv.prixVenteUnitaire - dv.prixAchatUnitaire)) AS profit_ligne
FROM detailventes dv
INNER JOIN produits p ON dv.produitId = p.id
WHERE dv.venteId = 1
ORDER BY dv.id_detail;

-- Get top selling products
SELECT 
    p.id,
    p.nom,
    p.codeBarre,
    SUM(dv.quantite) AS quantite_vendue,
    SUM(dv.quantite * dv.prixVenteUnitaire) AS chiffre_affaires,
    COUNT(DISTINCT dv.venteId) AS nombre_ventes
FROM detailventes dv
INNER JOIN produits p ON dv.produitId = p.id
GROUP BY p.id, p.nom, p.codeBarre
ORDER BY quantite_vendue DESC
LIMIT 20;

-- Get most profitable products
SELECT 
    p.id,
    p.nom,
    p.codeBarre,
    SUM(dv.quantite) AS quantite_vendue,
    SUM(dv.quantite * (dv.prixVenteUnitaire - dv.prixAchatUnitaire)) AS profit_total,
    AVG(dv.prixVenteUnitaire - dv.prixAchatUnitaire) AS profit_unitaire_moyen
FROM detailventes dv
INNER JOIN produits p ON dv.produitId = p.id
GROUP BY p.id, p.nom, p.codeBarre
ORDER BY profit_total DESC
LIMIT 20;

-- ============================================
-- 3. USER QUERIES
-- ============================================

-- Get all users
SELECT 
    id,
    username,
    role,
    DATE_FORMAT(date_creation, '%Y-%m-%d %H:%i:%s') AS date_creation
FROM utilisateurs
ORDER BY role, username;

-- Get sales statistics by user
SELECT 
    u.id,
    u.username,
    u.role,
    COUNT(v.id) AS nombre_ventes,
    SUM(v.totalVente) AS total_ventes,
    AVG(v.totalVente) AS moyenne_vente,
    MIN(v.dateVente) AS premiere_vente,
    MAX(v.dateVente) AS derniere_vente
FROM utilisateurs u
LEFT JOIN ventes v ON u.id = v.utilisateurId
GROUP BY u.id, u.username, u.role
ORDER BY total_ventes DESC;

-- ============================================
-- 4. INVENTORY & STOCK QUERIES
-- ============================================

-- Get stock movement (products sold vs current stock)
SELECT 
    p.id,
    p.nom,
    p.codeBarre,
    p.quantiteStock AS stock_actuel,
    COALESCE(SUM(dv.quantite), 0) AS quantite_vendue,
    (p.quantiteStock + COALESCE(SUM(dv.quantite), 0)) AS stock_initial_estime
FROM produits p
LEFT JOIN detailventes dv ON p.id = dv.produitId
GROUP BY p.id, p.nom, p.codeBarre, p.quantiteStock
ORDER BY quantite_vendue DESC;

-- Get products that haven't been sold
SELECT 
    p.id,
    p.codeBarre,
    p.nom,
    c.nom AS categorie,
    p.quantiteStock,
    p.prixVenteDefaut,
    p.date_derniere_maj
FROM produits p
LEFT JOIN categories c ON p.category_id = c.id
WHERE p.id NOT IN (
    SELECT DISTINCT produitId 
    FROM detailventes
)
ORDER BY p.nom;

-- ============================================
-- 5. FINANCIAL QUERIES
-- ============================================

-- Get total profit for a date range
SELECT 
    DATE(v.dateVente) AS date_vente,
    SUM(v.totalVente) AS chiffre_affaires,
    SUM(dv.quantite * dv.prixAchatUnitaire) AS cout_achat,
    SUM(dv.quantite * (dv.prixVenteUnitaire - dv.prixAchatUnitaire)) AS profit_brut,
    ROUND((SUM(dv.quantite * (dv.prixVenteUnitaire - dv.prixAchatUnitaire)) / SUM(v.totalVente)) * 100, 2) AS marge_pourcentage
FROM ventes v
INNER JOIN detailventes dv ON v.id = dv.venteId
WHERE v.dateVente BETWEEN '2024-01-01' AND '2024-12-31'
GROUP BY DATE(v.dateVente)
ORDER BY date_vente DESC;

-- Get overall financial summary
SELECT 
    COUNT(DISTINCT v.id) AS total_ventes,
    SUM(v.totalVente) AS chiffre_affaires_total,
    SUM(dv.quantite * dv.prixAchatUnitaire) AS cout_achat_total,
    SUM(dv.quantite * (dv.prixVenteUnitaire - dv.prixAchatUnitaire)) AS profit_brut_total,
    ROUND((SUM(dv.quantite * (dv.prixVenteUnitaire - dv.prixAchatUnitaire)) / SUM(v.totalVente)) * 100, 2) AS marge_pourcentage,
    AVG(v.totalVente) AS panier_moyen
FROM ventes v
INNER JOIN detailventes dv ON v.id = dv.venteId;

-- ============================================
-- 6. CATEGORY QUERIES
-- ============================================

-- Get category statistics
SELECT 
    c.id,
    c.nom AS categorie,
    COUNT(p.id) AS nombre_produits,
    SUM(p.quantiteStock) AS stock_total,
    SUM(p.quantiteStock * p.prixVenteDefaut) AS valeur_stock
FROM categories c
LEFT JOIN produits p ON c.id = p.category_id
GROUP BY c.id, c.nom
ORDER BY nombre_produits DESC;

-- Get sales by category
SELECT 
    c.nom AS categorie,
    COUNT(DISTINCT dv.venteId) AS nombre_ventes,
    SUM(dv.quantite) AS quantite_vendue,
    SUM(dv.quantite * dv.prixVenteUnitaire) AS chiffre_affaires,
    SUM(dv.quantite * (dv.prixVenteUnitaire - dv.prixAchatUnitaire)) AS profit
FROM categories c
INNER JOIN produits p ON c.id = p.category_id
INNER JOIN detailventes dv ON p.id = dv.produitId
GROUP BY c.id, c.nom
ORDER BY chiffre_affaires DESC;

-- ============================================
-- 7. MAINTENANCE QUERIES
-- ============================================

-- Check database integrity (orphaned records)
SELECT 'Ventes sans utilisateur' AS probleme, COUNT(*) AS nombre
FROM ventes v
LEFT JOIN utilisateurs u ON v.utilisateurId = u.id
WHERE u.id IS NULL

UNION ALL

SELECT 'Details de vente sans vente' AS probleme, COUNT(*) AS nombre
FROM detailventes dv
LEFT JOIN ventes v ON dv.venteId = v.id
WHERE v.id IS NULL

UNION ALL

SELECT 'Details de vente sans produit' AS probleme, COUNT(*) AS nombre
FROM detailventes dv
LEFT JOIN produits p ON dv.produitId = p.id
WHERE p.id IS NULL

UNION ALL

SELECT 'Produits sans categorie' AS probleme, COUNT(*) AS nombre
FROM produits p
WHERE p.category_id IS NULL AND p.categorie IS NULL OR p.categorie = '';

-- Get table sizes
SELECT 
    table_name AS table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb,
    table_rows AS nombre_lignes
FROM information_schema.TABLES
WHERE table_schema = '2market'
ORDER BY (data_length + index_length) DESC;

-- ============================================
-- 8. QUICK STATISTICS DASHBOARD
-- ============================================

-- Complete dashboard query
SELECT 
    'Statistiques Générales' AS section,
    (SELECT COUNT(*) FROM produits) AS total_produits,
    (SELECT COUNT(*) FROM utilisateurs) AS total_utilisateurs,
    (SELECT COUNT(*) FROM ventes) AS total_ventes,
    (SELECT SUM(totalVente) FROM ventes) AS chiffre_affaires_total,
    (SELECT COUNT(*) FROM produits WHERE quantiteStock <= seuilAlerte) AS produits_alerte,
    (SELECT SUM(quantiteStock) FROM produits) AS stock_total;

-- ============================================
-- 9. EXPORT QUERIES (for reports)
-- ============================================

-- Export all products for Excel
SELECT 
    p.codeBarre AS 'Code Barre',
    p.nom AS 'Nom Produit',
    c.nom AS 'Catégorie',
    p.prixAchatActuel AS 'Prix Achat',
    p.prixVenteDefaut AS 'Prix Vente',
    p.quantiteStock AS 'Stock',
    p.seuilAlerte AS 'Seuil Alerte',
    p.unite AS 'Unité'
FROM produits p
LEFT JOIN categories c ON p.category_id = c.id
ORDER BY c.nom, p.nom;

-- Export sales report
SELECT 
    v.id AS 'ID Vente',
    DATE_FORMAT(v.dateVente, '%Y-%m-%d %H:%i:%s') AS 'Date Vente',
    u.username AS 'Vendeur',
    p.nom AS 'Produit',
    p.codeBarre AS 'Code Barre',
    dv.quantite AS 'Quantité',
    dv.prixVenteUnitaire AS 'Prix Unitaire',
    (dv.quantite * dv.prixVenteUnitaire) AS 'Total Ligne',
    v.totalVente AS 'Total Vente'
FROM ventes v
INNER JOIN utilisateurs u ON v.utilisateurId = u.id
INNER JOIN detailventes dv ON v.id = dv.venteId
INNER JOIN produits p ON dv.produitId = p.id
ORDER BY v.dateVente DESC, v.id;

-- ============================================
-- END OF QUERIES
-- ============================================


