@echo off
REM Script batch pour installer les dépendances Maven

echo === Installation des dependances pour 2M Market ===
echo.

REM Vérifier si Maven est installé
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Maven est deja installe
    mvn -version
) else (
    echo.
    echo ERREUR: Maven n'est pas installe !
    echo.
    echo Options pour installer Maven:
    echo 1. Installer Chocolatey puis: choco install maven -y
    echo 2. Telecharger depuis https://maven.apache.org/download.cgi
    echo 3. Utiliser un IDE (IntelliJ IDEA, Eclipse) qui inclut Maven
    echo.
    pause
    exit /b 1
)

echo.
echo === Installation des dependances Maven ===
echo.

mvn clean install -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Toutes les dependances ont ete installees avec succes !
    echo.
    echo Prochaines etapes:
    echo 1. Creez la base de donnees en executant database/init_database.sql dans phpMyAdmin
    echo 2. Creez les utilisateurs: mvn compile exec:java -Dexec.mainClass="util.DatabaseSetup"
    echo 3. Lancez l'application: mvn javafx:run
) else (
    echo.
    echo ERREUR lors de l'installation des dependances
    pause
    exit /b 1
)

pause

