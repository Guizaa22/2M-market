# 📊 Structure des Tables - 2M Market

## Liste des Tables Requises

### ✅ **1. utilisateurs**
**Description :** Stocke les informations des utilisateurs (admin et employés)

**Colonnes :**
- `id` (INT, PRIMARY KEY, AUTO_INCREMENT)
- `username` (VARCHAR(50), UNIQUE, NOT NULL)
- `password_hash` (VARCHAR(255), NOT NULL) - Hash BCrypt
- `role` (ENUM('Admin', 'Employé'), NOT NULL)
- `date_creation` (TIMESTAMP)

**Index :** `idx_username`

---

### ✅ **2. categories** ⭐ NOUVELLE TABLE
**Description :** Stocke les catégories de produits (normalisée)

**Colonnes :**
- `id` (INT, PRIMARY KEY, AUTO_INCREMENT)
- `nom` (VARCHAR(100), UNIQUE, NOT NULL)
- `description` (TEXT)
- `date_creation` (TIMESTAMP)

**Index :** `idx_nom`

**Catégories par défaut :**

- Alimentaire
- Boissons
- Tabac
- Hygiène
- Divers

---

### ✅ **3. produits**
**Description :** Stocke les informations des produits

**Colonnes :**
- `id` (INT, PRIMARY KEY, AUTO_INCREMENT)
- `code_barre` (VARCHAR(50), UNIQUE, NOT NULL)
- `nom` (VARCHAR(100), NOT NULL)
- `categorie` (VARCHAR(50)) - ⚠️ Ancienne colonne (compatibilité)
- `category_id` (INT) - ⭐ Nouvelle colonne (recommandée)
- `prix_achat_actuel` (DECIMAL(10,2), NOT NULL)
- `prix_vente_defaut` (DECIMAL(10,2), NOT NULL)
- `quantite_stock` (INT, DEFAULT 0)
- `seuil_alerte` (INT, DEFAULT 10)
- `date_derniere_maj` (TIMESTAMP)

**Index :**
- `idx_code_barre`
- `idx_nom`
- `idx_category_id`

**Clés étrangères :**
- `category_id` → `categories(id)` (ON DELETE SET NULL)

---

### ✅ **4. ventes**
**Description :** Stocke les informations des ventes

**Colonnes :**
- `id` (INT, PRIMARY KEY, AUTO_INCREMENT)
- `id_utilisateur` (INT, NOT NULL)
- `date_vente` (DATETIME, NOT NULL)
- `total_vente` (DECIMAL(10,2), NOT NULL)
- `type_paiement` (ENUM('Espèces', 'Carte', 'Autre'), DEFAULT 'Espèces')
- `date_creation` (TIMESTAMP)

**Index :**
- `idx_date_vente`
- `idx_id_utilisateur`

**Clés étrangères :**
- `id_utilisateur` → `utilisateurs(id)` (ON DELETE RESTRICT)

---

### ✅ **5. detailsvente**
**Description :** Stocke les détails de chaque vente (produits vendus)

**Colonnes :**
- `id` (INT, PRIMARY KEY, AUTO_INCREMENT)
- `id_vente` (INT, NOT NULL)
- `id_produit` (INT, NOT NULL)
- `quantite` (INT, NOT NULL)
- `prix_vente_unitaire` (DECIMAL(10,2), NOT NULL)
- `prix_achat_unitaire` (DECIMAL(10,2), NOT NULL)

**Index :**
- `idx_id_vente`
- `idx_id_produit`

**Clés étrangères :**
- `id_vente` → `ventes(id)` (ON DELETE CASCADE)
- `id_produit` → `produits(id)` (ON DELETE RESTRICT)

---

## 📋 Schéma Relationnel

```
utilisateurs (1) ──< (N) ventes
                      │
                      └──< (N) detailsvente
                              │
                              └──> (1) produits
                                      │
                                      └──> (N) categories
```

## 🚀 Installation

### Option 1 : Script Complet (Recommandé)
Exécutez le fichier `COMPLETE_DATABASE_STRUCTURE.sql` dans phpMyAdmin

### Option 2 : Table par Table
1. Créer `utilisateurs`
2. Créer `categories` ⭐
3. Créer `produits`
4. Créer `ventes`
5. Créer `detailsvente`

## ⚠️ Notes Importantes

1. **Table `categories` :** Nouvelle table recommandée pour la normalisation
2. **Colonne `category_id` :** Utilisez cette colonne au lieu de `categorie` (VARCHAR)
3. **Compatibilité :** Le code supporte les deux approches (ancienne et nouvelle)
4. **Migration :** Utilisez `add_categories_table.sql` pour migrer les données existantes

## 🔍 Vérification

Après création, vérifiez avec :
```sql
SHOW TABLES;
DESCRIBE utilisateurs;
DESCRIBE categories;
DESCRIBE produits;
DESCRIBE ventes;
DESCRIBE detailsvente;
```

