# Script pour telecharger et installer Maven automatiquement
Write-Host "=== Installation automatique de Maven ===" -ForegroundColor Cyan

$mavenVersion = "3.9.6"
$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
$installDir = "$env:USERPROFILE\maven"
$zipFile = "$env:TEMP\maven-$mavenVersion.zip"

Write-Host "Version: $mavenVersion" -ForegroundColor Yellow
Write-Host "Repertoire d'installation: $installDir" -ForegroundColor Yellow

# Creer le repertoire
if (-not (Test-Path $installDir)) {
    New-Item -ItemType Directory -Path $installDir -Force | Out-Null
}

# Telecharger
Write-Host "`nTelechargement de Maven..." -ForegroundColor Yellow
try {
    $ProgressPreference = 'SilentlyContinue'
    Invoke-WebRequest -Uri $mavenUrl -OutFile $zipFile -UseBasicParsing
    Write-Host "Telechargement termine" -ForegroundColor Green
} catch {
    Write-Host "ERREUR: Impossible de telecharger Maven" -ForegroundColor Red
    Write-Host "Erreur: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "`nVeuillez telecharger manuellement depuis:" -ForegroundColor Yellow
    Write-Host "https://maven.apache.org/download.cgi" -ForegroundColor Cyan
    exit 1
}

# Extraire
Write-Host "Extraction de Maven..." -ForegroundColor Yellow
try {
    Expand-Archive -Path $zipFile -DestinationPath $installDir -Force
    Write-Host "Extraction terminee" -ForegroundColor Green
} catch {
    Write-Host "ERREUR: Impossible d'extraire Maven" -ForegroundColor Red
    exit 1
}

$mavenHome = Join-Path $installDir "apache-maven-$mavenVersion"
$mavenBin = Join-Path $mavenHome "bin"

Write-Host "`nMaven installe dans: $mavenHome" -ForegroundColor Green

# Ajouter temporairement au PATH pour cette session
$env:PATH = "$mavenBin;$env:PATH"

# Verifier l'installation
Write-Host "`nVerification de l'installation..." -ForegroundColor Yellow
$mvnVersion = & "$mavenBin\mvn.cmd" -version 2>&1 | Select-String "Apache Maven"
if ($mvnVersion) {
    Write-Host "Maven fonctionne correctement!" -ForegroundColor Green
    Write-Host $mvnVersion -ForegroundColor Cyan
    
    # Installer les dependances
    Write-Host "`nInstallation des dependances du projet..." -ForegroundColor Yellow
    & "$mavenBin\mvn.cmd" clean install
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n=== SUCCES ===" -ForegroundColor Green
        Write-Host "Dependances installees avec succes!" -ForegroundColor Green
        Write-Host "`nPour utiliser Maven a l'avenir:" -ForegroundColor Yellow
        Write-Host "1. Ajoutez au PATH Windows: $mavenBin" -ForegroundColor Cyan
        Write-Host "2. Ou utilisez directement: $mavenBin\mvn.cmd" -ForegroundColor Cyan
    } else {
        Write-Host "`nErreur lors de l'installation des dependances" -ForegroundColor Red
    }
} else {
    Write-Host "ERREUR: Maven ne fonctionne pas correctement" -ForegroundColor Red
}

# Nettoyer
Remove-Item $zipFile -ErrorAction SilentlyContinue

