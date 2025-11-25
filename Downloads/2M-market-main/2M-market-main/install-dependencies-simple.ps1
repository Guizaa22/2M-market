# Script simple pour installer les dependances (si Maven est deja installe)
Write-Host "Verification de Maven..." -ForegroundColor Green

# Verifier Java
$javaCheck = java -version 2>&1
if (-not $javaCheck) {
    Write-Host "ERREUR: Java n'est pas installe" -ForegroundColor Red
    exit 1
}
Write-Host "Java OK" -ForegroundColor Green

# Chercher Maven dans les emplacements communs
$mavenPaths = @(
    "$env:ProgramFiles\Apache\maven\bin\mvn.cmd",
    "$env:ProgramFiles(x86)\Apache\maven\bin\mvn.cmd",
    "$env:USERPROFILE\.m2\maven\apache-maven-*\bin\mvn.cmd",
    "C:\maven\bin\mvn.cmd"
)

$mvnCmd = $null
foreach ($path in $mavenPaths) {
    $resolved = Resolve-Path $path -ErrorAction SilentlyContinue
    if ($resolved) {
        $mvnCmd = $resolved[0].Path
        break
    }
}

# Essayer aussi via PATH
if (-not $mvnCmd) {
    $mvnInPath = Get-Command mvn -ErrorAction SilentlyContinue
    if ($mvnInPath) {
        $mvnCmd = $mvnInPath.Path
    }
}

if ($mvnCmd) {
    Write-Host "Maven trouve: $mvnCmd" -ForegroundColor Green
    Write-Host "Installation des dependances..." -ForegroundColor Yellow
    & $mvnCmd clean install
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`nDependances installees avec succes!" -ForegroundColor Green
    } else {
        Write-Host "`nErreur lors de l'installation" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "`nMaven n'est pas trouve sur ce systeme" -ForegroundColor Red
    Write-Host "`nVeuillez installer Maven:" -ForegroundColor Yellow
    Write-Host "1. Telecharger depuis https://maven.apache.org/download.cgi" -ForegroundColor Cyan
    Write-Host "2. Extraire dans un dossier (ex: C:\Program Files\Apache\maven)" -ForegroundColor Cyan
    Write-Host "3. Ajouter le dossier \bin au PATH Windows" -ForegroundColor Cyan
    Write-Host "4. Redemarrer le terminal et relancer ce script" -ForegroundColor Cyan
    Write-Host "`nOu consultez INSTALL_MAVEN.md pour plus de details" -ForegroundColor Cyan
    exit 1
}

