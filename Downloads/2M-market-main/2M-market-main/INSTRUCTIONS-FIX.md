# 🔧 INSTRUCTIONS POUR CORRIGER LE PROBLÈME DE CONNEXION

## ❌ Problème identifié

Le mot de passe dans la base de données est stocké **en clair** ("password123") au lieu d'être un **hash BCrypt**.

## ✅ Solution

### Étape 1 : Ouvrir phpMyAdmin
1. Ouvrez votre navigateur
2. Allez sur http://localhost/phpmyadmin

### Étape 2 : Exécuter le script SQL
1. Cliquez sur la base de données **`2market`** dans le menu de gauche
2. Cliquez sur l'onglet **"SQL"** en haut
3. **Copiez-collez** le contenu du fichier **`FIX-PASSWORD-NOW.sql`**
4. Cliquez sur **"Exécuter"**

### Étape 3 : Vérifier
Après l'exécution, vous devriez voir :
- Le hash du mot de passe commence par `$2a$10$...`
- L'utilisateur admin est mis à jour

### Étape 4 : Se connecter
1. Relancez l'application
2. Utilisez :
   - **Username** : `admin`
   - **Password** : `admin123`

## 📋 Contenu du script SQL

```sql
USE 2market;

UPDATE utilisateurs 
SET password_hash = '$2a$10$Xu3nfFWNbDsB2jLtomPEu.OzF0PjZ7yTSX5e3e7FwA39.UEXv9SV.' 
WHERE username = 'admin';
```

---

**⚠️ IMPORTANT** : Le script doit être exécuté dans phpMyAdmin pour que la connexion fonctionne !

