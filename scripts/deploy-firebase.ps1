# Build frontend and deploy to Firebase Hosting.
# Prerequisites: Firebase CLI (npm install -g firebase-tools), firebase login, firebase use --add
# Usage: from project root: .\scripts\deploy-firebase.ps1
#        or from frontend:  npm run deploy

$ErrorActionPreference = "Stop"
$scriptDir = $PSScriptRoot
$projectRoot = Split-Path -Parent $scriptDir
Set-Location $projectRoot

Write-Host "Building frontend..." -ForegroundColor Cyan
Set-Location "$projectRoot\frontend"
if (-not (Test-Path "node_modules")) {
    npm install
}
npm run build
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Set-Location $projectRoot
Write-Host "Deploying to Firebase Hosting..." -ForegroundColor Cyan
firebase deploy
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Done. Check the Hosting URL in the output above." -ForegroundColor Green
