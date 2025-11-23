# Guide de Démarrage Rapide

## 🚀 Installation en 5 étapes

### 1. Prérequis
- ✅ Java JDK 17+ installé
- ✅ Maven installé
- ✅ XAMPP installé et MySQL démarré

### 2. Installer les dépendances
```bash
mvn clean install
```

### 3. Créer la base de données
1. Ouvrir phpMyAdmin (http://localhost/phpmyadmin)
2. Cliquer sur l'onglet "SQL"
3. Copier-coller le contenu de `database/init_database.sql`
4. Cliquer sur "Exécuter"

### 4. Créer les utilisateurs par défaut
```bash
mvn compile exec:java -Dexec.mainClass="util.DatabaseSetup"
```

### 5. Lancer l'application
```bash
mvn javafx:run
```

## 🔑 Comptes par défaut

- **Admin** : `admin` / `admin123`
- **Employé** : `employe` / `admin123`

## ⚠️ Problèmes courants

**Erreur de connexion MySQL ?**
- Vérifiez que MySQL est démarré dans XAMPP
- Vérifiez `src/main/java/util/Config.java`

**Dépendances non trouvées ?**
- Exécutez `mvn clean install` à nouveau

**Utilisateurs non créés ?**
- Exécutez `DatabaseSetup` manuellement

## 📖 Documentation complète

Voir `README.md` pour plus de détails.

