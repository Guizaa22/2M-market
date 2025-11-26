# Script PowerShell pour configurer le dépôt Git distant
# Usage: .\setup-git-remote.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Configuration du Dépôt Git Distant" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Demander l'URL du dépôt
$repoUrl = Read-Host "Entrez l'URL de votre dépôt Git (ex: https://github.com/username/repo.git)"

if ([string]::IsNullOrWhiteSpace($repoUrl)) {
    Write-Host "URL invalide. Opération annulée." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Configuration du remote 'origin'..." -ForegroundColor Yellow

# Vérifier si un remote existe déjà
$existingRemote = git remote get-url origin 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "Un remote 'origin' existe déjà: $existingRemote" -ForegroundColor Yellow
    $replace = Read-Host "Voulez-vous le remplacer? (O/N)"
    if ($replace -eq "O" -or $replace -eq "o") {
        git remote set-url origin $repoUrl
        Write-Host "Remote 'origin' mis à jour." -ForegroundColor Green
    } else {
        Write-Host "Opération annulée." -ForegroundColor Yellow
        exit 0
    }
} else {
    git remote add origin $repoUrl
    Write-Host "Remote 'origin' ajouté avec succès." -ForegroundColor Green
}

Write-Host ""
Write-Host "Récupération des mises à jour depuis le dépôt distant..." -ForegroundColor Yellow

# Essayer de pull
git pull origin master --allow-unrelated-histories 2>&1 | Out-String
if ($LASTEXITCODE -eq 0) {
    Write-Host "Mises à jour récupérées avec succès!" -ForegroundColor Green
} else {
    Write-Host "Aucune mise à jour disponible ou erreur lors du pull." -ForegroundColor Yellow
    Write-Host "Vous pouvez maintenant pousser vos modifications avec:" -ForegroundColor Cyan
    Write-Host "  git push -u origin master" -ForegroundColor White
}

Write-Host ""
Write-Host "Vérification du remote configuré:" -ForegroundColor Cyan
git remote -v

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Configuration terminée!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan


