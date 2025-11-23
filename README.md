# 2M Market - Application de Gestion de Stock

Application desktop Java/JavaFX pour la gestion de stock et de ventes d'un marché (denrées alimentaires et tabac).

## 📋 Fonctionnalités

### Interface Administrateur
- **Gestion de Stock** : Ajout, modification et suppression de produits
- Gestion des produits par code-barres
- Suivi des quantités en stock et seuils d'alerte
- Recherche de produits

### Interface Employé
- **Point de Vente (Caisse)** : Enregistrement des ventes
- Scanner de code-barres pour ajouter des produits
- Calcul automatique du total
- Gestion du panier de vente

### Sécurité
- Authentification par nom d'utilisateur et mot de passe
- Hachage des mots de passe avec BCrypt
- Rôles : Admin et Employé

## 🛠️ Technologies Utilisées

- **Java 17+**
- **JavaFX 21**
- **MySQL** (via XAMPP)
- **Maven** (gestion des dépendances)
- **BCrypt** (hachage de mots de passe)

## 📦 Prérequis

1. **Java JDK 17 ou supérieur**
   - Télécharger depuis [Oracle](https://www.oracle.com/java/technologies/downloads/) ou [OpenJDK](https://openjdk.org/)

2. **Maven 3.6+**
   - Télécharger depuis [Apache Maven](https://maven.apache.org/download.cgi)
   - Ou installer via votre gestionnaire de paquets

3. **XAMPP** (MySQL + phpMyAdmin)
   - Télécharger depuis [Apache Friends](https://www.apachefriends.org/)
   - Installer et démarrer MySQL dans XAMPP Control Panel

## 🚀 Installation et Configuration

### 1. Configuration de la Base de Données

1. **Démarrer XAMPP**
   - Ouvrir XAMPP Control Panel
   - Démarrer le service **MySQL**

2. **Créer la Base de Données**
   - Ouvrir phpMyAdmin (http://localhost/phpmyadmin)
   - Ou utiliser la ligne de commande MySQL
   - Exécuter le script `database/init_database.sql`

   **Via phpMyAdmin :**
   - Cliquer sur l'onglet "SQL"
   - Copier-coller le contenu de `database/init_database.sql`
   - Cliquer sur "Exécuter"

   **Via ligne de commande :**
   ```bash
   mysql -u root -p < database/init_database.sql
   ```

3. **Créer les Utilisateurs par Défaut**
   - Après avoir créé la base de données, exécutez la classe `DatabaseSetup` pour créer les utilisateurs :
   ```bash
   mvn compile exec:java -Dexec.mainClass="util.DatabaseSetup"
   ```
   - Ou compilez et exécutez manuellement :
   ```bash
   mvn compile
   java -cp target/classes util.DatabaseSetup
   ```

4. **Vérifier la Configuration**
   - Ouvrir `src/main/java/util/Config.java`
   - Vérifier que les paramètres de connexion correspondent à votre installation MySQL :
     - `DB_URL` : `jdbc:mysql://localhost:3306/2market`
     - `DB_USER` : `root` (par défaut)
     - `DB_PASSWORD` : `` (vide par défaut, ou votre mot de passe MySQL)

### 2. Installation des Dépendances

```bash
# Dans le répertoire du projet
mvn clean install
```

Cela téléchargera automatiquement toutes les dépendances nécessaires (JavaFX, MySQL Connector, BCrypt).

### 3. Compilation et Exécution

**Option 1 : Via Maven (recommandé)**
```bash
# Compiler et exécuter
mvn javafx:run
```

**Option 2 : Via IDE (IntelliJ IDEA, Eclipse, etc.)**
1. Importer le projet comme projet Maven
2. Attendre que Maven télécharge les dépendances
3. Exécuter la classe `app.MainApp`

**Option 3 : Créer un JAR exécutable**
```bash
# Créer un JAR avec toutes les dépendances
mvn clean package

# Exécuter le JAR
java -jar target/2M-market-1.0-SNAPSHOT.jar
```

## 👤 Comptes par Défaut

Après l'initialisation de la base de données, deux comptes sont créés :

### Administrateur
- **Nom d'utilisateur** : `admin`
- **Mot de passe** : `admin123`
- **Rôle** : Admin

### Employé
- **Nom d'utilisateur** : `employe`
- **Mot de passe** : `admin123`
- **Rôle** : Employé

⚠️ **Important** : Changez ces mots de passe en production !

## 📁 Structure du Projet

```
2M_market/
├── src/
│   └── main/
│       ├── java/
│       │   ├── app/
│       │   │   └── MainApp.java          # Point d'entrée de l'application
│       │   ├── controller/
│       │   │   ├── ConnexionController.java
│       │   │   ├── AdminDashboardController.java
│       │   │   ├── GestionStockController.java
│       │   │   └── CaisseController.java
│       │   ├── dao/
│       │   │   ├── DBConnector.java      # Connexion MySQL
│       │   │   ├── ProduitDAO.java
│       │   │   ├── UtilisateurDAO.java
│       │   │   └── VenteDAO.java
│       │   ├── model/
│       │   │   ├── Produit.java
│       │   │   ├── Utilisateur.java
│       │   │   ├── Vente.java
│       │   │   └── DetailVente.java
│       │   ├── util/
│       │   │   ├── Config.java           # Configuration DB
│       │   │   ├── FXMLUtils.java
│       │   │   └── SecurityUtil.java     # Hachage BCrypt
│       │   └── view/
│       │       └── MainView.java
│       └── resources/
│           └── view/
│               ├── Connexion.fxml
│               ├── AdminDashboard.fxml
│               ├── GestionStock.fxml
│               └── Caisse.fxml
├── database/
│   └── init_database.sql                  # Script d'initialisation DB
├── pom.xml                                # Configuration Maven
└── README.md
```

## 🔧 Dépannage

### Erreur de connexion à la base de données
- Vérifier que MySQL est démarré dans XAMPP
- Vérifier les paramètres dans `Config.java`
- Vérifier que la base de données `2market` existe

### Erreur "Driver MySQL non trouvé"
- Vérifier que Maven a bien téléchargé les dépendances : `mvn clean install`

### Erreur JavaFX
- Vérifier que Java 17+ est installé : `java -version`
- Vérifier que le module JavaFX est bien dans les dépendances Maven

### L'application ne démarre pas
- Vérifier les logs dans la console
- Vérifier que toutes les dépendances sont installées : `mvn dependency:resolve`

## 📝 Notes

- Les produits sont gérés uniquement par les administrateurs
- Les employés peuvent uniquement effectuer des ventes
- Le stock est automatiquement déduit lors d'une vente
- Les produits avec un stock inférieur au seuil d'alerte sont mis en évidence

## 📄 Licence

Ce projet est fourni à des fins éducatives.

## 👨‍💻 Support

Pour toute question ou problème, vérifiez :
1. Que tous les prérequis sont installés
2. Que la base de données est correctement configurée
3. Que les dépendances Maven sont téléchargées

---

**Bon développement ! 🚀**

