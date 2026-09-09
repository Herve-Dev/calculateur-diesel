# release.ps1 - Script d'automatisation de release pour DieselCalculateur

$ErrorActionPreference = "Stop"

# 1. Avertissement de securite si des fichiers non commites sont presents
$gitStatus = git status --porcelain
if ($gitStatus) {
    Write-Host "[AVERTISSEMENT] Des modifications non commitees sont presentes dans le depot :" -ForegroundColor Yellow
    Write-Host $gitStatus -ForegroundColor DarkGray
    Write-Host "Ces modifications seront incluses dans le commit de release." -ForegroundColor Yellow
    Write-Host ""
}

# 2. Lecture automatique du versionCode et versionName depuis app/build.gradle.kts
$gradleFile = "app/build.gradle.kts"

if (-not (Test-Path $gradleFile)) {
    Write-Error "Fichier introuvable : $gradleFile"
    exit 1
}

$gradleContent = Get-Content -Path $gradleFile -Raw -Encoding UTF8

if ($gradleContent -match 'versionCode\s*=\s*(\d+)') {
    $currentVersionCode = [int]$Matches[1]
} else {
    Write-Error "Impossible de trouver 'versionCode' dans $gradleFile"
    exit 1
}

if ($gradleContent -match 'versionName\s*=\s*"([^"]+)"') {
    $currentVersionName = $Matches[1]
} else {
    Write-Error "Impossible de trouver 'versionName' dans $gradleFile"
    exit 1
}

Write-Host "=== Workflow de Release ===" -ForegroundColor Cyan
Write-Host "Version actuelle :" -ForegroundColor White
Write-Host "  - versionName : $currentVersionName"
Write-Host "  - versionCode : $currentVersionCode"
Write-Host ""

# 3. Choix du type de bump
$bumpTypeInput = Read-Host "Type de bump (patch [defaut], minor, major)"
if ([string]::IsNullOrWhiteSpace($bumpTypeInput)) {
    $bumpType = "patch"
} else {
    $bumpType = $bumpTypeInput.Trim().ToLower()
}

# Decoupage du versionName en SemVer (X.Y.Z)
$parts = $currentVersionName.Split('.')
$major = if ($parts.Length -gt 0) { [int]$parts[0] } else { 0 }
$minor = if ($parts.Length -gt 1) { [int]$parts[1] } else { 0 }
$patch = if ($parts.Length -gt 2) { [int]$parts[2] } else { 0 }

switch -Wildcard ($bumpType) {
    "major*" {
        $major++
        $minor = 0
        $patch = 0
    }
    "minor*" {
        $minor++
        $patch = 0
    }
    "patch*" {
        $patch++
    }
    "p*" {
        $patch++
    }
    "m*" {
        $minor++
        $patch = 0
    }
    "maj*" {
        $major++
        $minor = 0
        $patch = 0
    }
    default {
        Write-Host "Type '$bumpType' non reconnu. Application du bump par defaut : 'patch'." -ForegroundColor Yellow
        $patch++
    }
}

$newVersionName = "$major.$minor.$patch"
$newVersionCode = $currentVersionCode + 1

Write-Host ""
Write-Host "Nouvelle version calculee :" -ForegroundColor Green
Write-Host "  - versionName : $newVersionName"
Write-Host "  - versionCode : $newVersionCode"
Write-Host "  - Tag Git     : v$newVersionName"
Write-Host ""

# 4. Confirmation avant continuation
$confirm = Read-Host "Confirmer et publier cette version ? (O/N) [defaut: O]"
if ([string]::IsNullOrWhiteSpace($confirm)) {
    $confirm = "O"
}

if ($confirm.Trim().ToUpper() -notin @("O", "OUI", "Y", "YES")) {
    Write-Host "Operation annulee." -ForegroundColor Yellow
    exit 0
}

# 5. Demande du message de commit
$defaultCommitMsg = "Release v$newVersionName"
$commitMsg = Read-Host "Message de commit [defaut: '$defaultCommitMsg']"
if ([string]::IsNullOrWhiteSpace($commitMsg)) {
    $commitMsg = $defaultCommitMsg
}

# 6. Modification de app/build.gradle.kts
$updatedContent = $gradleContent -replace '(?<=\bversionCode\s*=\s*)\d+', "$newVersionCode"
$updatedContent = $updatedContent -replace '(?<=\bversionName\s*=\s*)"[^"]+"', "`"$newVersionName`""

[System.IO.File]::WriteAllText((Resolve-Path $gradleFile).Path, $updatedContent, [System.Text.UTF8Encoding]::new($false))

Write-Host "Fichier $gradleFile mis a jour." -ForegroundColor Green

# 6b. Verification du build local (assembleRelease)
Write-Host ""
Write-Host "Verification du build local (assembleRelease)..." -ForegroundColor Cyan

# Configuration du JBR pour Gradle
$env:JAVA_HOME = 'G:\Android\Android Studio\jbr'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "> .\gradlew.bat assembleRelease" -ForegroundColor DarkGray
.\gradlew.bat assembleRelease

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "[ERREUR] Le build local (assembleRelease) a echoue !" -ForegroundColor Red
    Write-Host "Restauration du fichier $gradleFile a son etat d'origine..." -ForegroundColor Yellow

    [System.IO.File]::WriteAllText((Resolve-Path $gradleFile).Path, $gradleContent, [System.Text.UTF8Encoding]::new($false))

    Write-Host "Le bump de version a ete annule. Aucune modification Git n'a ete effectuee." -ForegroundColor Yellow
    exit 1
}

Write-Host "Build verifie avec succes !" -ForegroundColor Green

# 7. Execution des commandes Git
Write-Host ""
Write-Host "Execution des commandes Git..." -ForegroundColor Cyan

Write-Host "> git add -A" -ForegroundColor DarkGray
git add -A
if ($LASTEXITCODE -ne 0) { throw "Echec de 'git add -A'" }

Write-Host "> git commit -m `"$commitMsg`"" -ForegroundColor DarkGray
git commit -m "$commitMsg"
if ($LASTEXITCODE -ne 0) { throw "Echec de 'git commit'" }

Write-Host "> git push origin main" -ForegroundColor DarkGray
git push origin main
if ($LASTEXITCODE -ne 0) { throw "Echec de 'git push origin main'" }

$tagName = "v$newVersionName"
Write-Host "> git tag $tagName" -ForegroundColor DarkGray
git tag $tagName
if ($LASTEXITCODE -ne 0) { throw "Echec de 'git tag $tagName'" }

Write-Host "> git push origin $tagName" -ForegroundColor DarkGray
git push origin $tagName
if ($LASTEXITCODE -ne 0) { throw "Echec de 'git push origin $tagName'" }

# 8. Recuperation de l'URL Actions pour le resume
$actionsUrl = ""
try {
    $remoteUrl = (git remote get-url origin).Trim()
    if ($remoteUrl) {
        $repoUrl = $remoteUrl -replace '^git@github\.com:', 'https://github.com/'
        $repoUrl = $repoUrl -replace '\.git$', ''
        $actionsUrl = "$repoUrl/actions"
    }
} catch {
    # Ignorer en cas d'erreur
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "       RELEASE PUBLIEE AVEC SUCCES        " -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host " - Version Name : $newVersionName"
Write-Host " - Version Code : $newVersionCode"
Write-Host " - Tag Git     : $tagName"
Write-Host " - Commit       : $commitMsg"
if ($actionsUrl) {
    Write-Host ""
    Write-Host "Suivez le workflow dans l'onglet Actions de votre depot :" -ForegroundColor Cyan
    Write-Host " $actionsUrl" -ForegroundColor Yellow
}
Write-Host "==========================================" -ForegroundColor Green
