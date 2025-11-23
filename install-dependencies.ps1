# Script PowerShell pour installer les dépendances Maven
Write-Host "Installation des dépendances Maven..." -ForegroundColor Green

# Vérifier si Java est installé
$javaVersion = java -version 2>&1 | Select-String "version"
if (-not $javaVersion) {
    Write-Host "ERREUR: Java n'est pas installé ou n'est pas dans le PATH" -ForegroundColor Red
    exit 1
}
Write-Host "Java trouvé: $javaVersion" -ForegroundColor Green

# Vérifier si Maven est disponible
$mvnPath = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvnPath) {
    Write-Host "Maven trouvé: $($mvnPath.Path)" -ForegroundColor Green
    Write-Host "Installation des dépendances..." -ForegroundColor Yellow
    mvn clean install
    exit 0
}

# Si Maven n'est pas trouvé, essayer de le télécharger
Write-Host "Maven n'est pas trouvé dans le PATH" -ForegroundColor Yellow
Write-Host "Tentative de téléchargement de Maven..." -ForegroundColor Yellow

$mavenVersion = "3.9.5"
$mavenUrl = "https://dlcdn.apache.org/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
$mavenDir = "$env:USERPROFILE\.m2\maven"
$mavenZip = "$env:TEMP\maven.zip"

# Créer le répertoire Maven
if (-not (Test-Path $mavenDir)) {
    New-Item -ItemType Directory -Path $mavenDir -Force | Out-Null
}

# Télécharger Maven
Write-Host "Téléchargement de Maven $mavenVersion..." -ForegroundColor Yellow
try {
    Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip -UseBasicParsing
    Write-Host "Téléchargement terminé" -ForegroundColor Green
} catch {
    Write-Host "ERREUR: Impossible de télécharger Maven" -ForegroundColor Red
    Write-Host "Veuillez installer Maven manuellement depuis https://maven.apache.org/download.cgi" -ForegroundColor Yellow
    Write-Host "Ou ajoutez Maven à votre PATH" -ForegroundColor Yellow
    exit 1
}

# Extraire Maven
Write-Host "Extraction de Maven..." -ForegroundColor Yellow
Expand-Archive -Path $mavenZip -DestinationPath $mavenDir -Force
$mavenHome = Join-Path $mavenDir "apache-maven-$mavenVersion"
$mavenBin = Join-Path $mavenHome "bin"

# Utiliser Maven pour installer les dépendances
$env:PATH = "$mavenBin;$env:PATH"
Write-Host "Installation des dépendances avec Maven..." -ForegroundColor Yellow
& "$mavenBin\mvn.cmd" clean install

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nDependances installees avec succes!" -ForegroundColor Green
    Write-Host "Maven a été installé dans: $mavenHome" -ForegroundColor Cyan
    Write-Host "Pour utiliser Maven à l'avenir, ajoutez à votre PATH: $mavenBin" -ForegroundColor Cyan
} else {
    Write-Host "`nErreur lors de l'installation des dependances" -ForegroundColor Red
    exit 1
}
