# Installation de Maven

Maven n'est pas installé sur votre système. Voici plusieurs méthodes pour l'installer :

## Méthode 1 : Installation manuelle (Recommandée)

1. **Télécharger Maven**
   - Aller sur https://maven.apache.org/download.cgi
   - Télécharger `apache-maven-3.9.5-bin.zip` (ou version plus récente)

2. **Extraire Maven**
   - Extraire le fichier ZIP dans un dossier (ex: `C:\Program Files\Apache\maven`)

3. **Ajouter Maven au PATH**
   - Ouvrir "Variables d'environnement" dans Windows
   - Ajouter `C:\Program Files\Apache\maven\bin` (ou votre chemin) à la variable PATH
   - Redémarrer le terminal

4. **Vérifier l'installation**
   ```bash
   mvn -version
   ```

## Méthode 2 : Via Chocolatey (si installé)

```powershell
choco install maven
```

## Méthode 3 : Via Scoop (si installé)

```powershell
scoop install maven
```

## Après l'installation

Une fois Maven installé, exécutez dans le répertoire du projet :

```bash
mvn clean install
```

Cela téléchargera automatiquement toutes les dépendances (JavaFX, MySQL Connector, BCrypt).

