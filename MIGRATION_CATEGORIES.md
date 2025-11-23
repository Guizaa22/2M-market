# Migration vers Table Catégories

## Pourquoi utiliser une table séparée ?

✅ **Avantages :**
- Normalisation de la base de données
- Évite les doublons et erreurs de saisie
- Gestion centralisée des catégories
- Possibilité d'ajouter des métadonnées (description, icône, etc.)
- Meilleure performance avec index

## Comment migrer ?

### Option 1 : Migration automatique (Recommandé)

1. **Exécuter le script SQL** dans phpMyAdmin :
   ```sql
   -- Ouvrir database/add_categories_table.sql et exécuter dans phpMyAdmin
   ```

2. **Le script va :**
   - Créer la table `categories`
   - Ajouter la colonne `category_id` à `produits`
   - Migrer automatiquement les données existantes
   - Créer les catégories par défaut

### Option 2 : Migration manuelle

1. **Créer la table categories :**
   ```sql
   CREATE TABLE categories (
       id INT AUTO_INCREMENT PRIMARY KEY,
       nom VARCHAR(100) NOT NULL UNIQUE,
       description TEXT,
       date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   ```

2. **Ajouter la colonne category_id :**
   ```sql
   ALTER TABLE produits 
   ADD COLUMN category_id INT NULL,
   ADD FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;
   ```

3. **Insérer les catégories :**
   ```sql
   INSERT INTO categories (nom, description) VALUES
   ('Alimentaire', 'Produits alimentaires'),
   ('Boissons', 'Boissons et liquides'),
   ('Tabac', 'Produits de tabac'),
   ('Hygiène', 'Produits d''hygiène'),
   ('Divers', 'Autres produits');
   ```

4. **Migrer les données :**
   ```sql
   UPDATE produits p
   LEFT JOIN categories c ON LOWER(TRIM(p.categorie)) = LOWER(TRIM(c.nom))
   SET p.category_id = c.id
   WHERE p.categorie IS NOT NULL;
   ```

## Code mis à jour

Le code supporte maintenant **les deux approches** :
- ✅ Si la table `categories` existe → utilise `category_id`
- ✅ Sinon → utilise la colonne `categorie` (VARCHAR)

**Aucun changement nécessaire dans votre code Java !** Le code détecte automatiquement quelle méthode utiliser.

## Vérification

Après migration, vérifiez :
```sql
-- Vérifier les catégories créées
SELECT * FROM categories;

-- Vérifier les produits migrés
SELECT COUNT(*) as total, 
       COUNT(category_id) as avec_categorie,
       COUNT(*) - COUNT(category_id) as sans_categorie
FROM produits;
```

## Gestion des catégories

Une fois la table créée, vous pouvez :
- Créer de nouvelles catégories via `CategorieDAO`
- Modifier les catégories existantes
- Supprimer des catégories (les produits auront category_id = NULL)

