# Deploy the app to production: build frontend with production API URL and deploy to Firebase.
# Backend and DB are already on Cloud Run + Neon. Run set-production-env-cloudrun.ps1 once to configure the backend.
#
# Prerequisites: Firebase CLI, firebase use (project linked). Node in frontend/.
#
# Usage (from project root):
#   .\scripts\deploy-production.ps1

$ErrorActionPreference = "Stop"
$scriptDir = $PSScriptRoot
$projectRoot = Split-Path -Parent $scriptDir
Set-Location $projectRoot

Write-Host "Production deploy: frontend -> Firebase Hosting (API: Cloud Run)" -ForegroundColor Cyan
Write-Host ""

# Vite uses .env.production when building for production (npm run build)
Set-Location "$projectRoot\frontend"
if (-not (Test-Path "node_modules")) {
    npm install
}
if (-not (Test-Path ".env.production")) {
    Write-Error "Missing frontend/.env.production. It should set VITE_API_URL to your Cloud Run backend URL (e.g. https://....run.app/api)"
    exit 1
}
npm run build
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Set-Location $projectRoot
Write-Host ""
Write-Host "Deploying to Firebase Hosting..." -ForegroundColor Cyan
firebase deploy
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host ""
Write-Host "Done. Frontend is live on Firebase; it will call the backend at the URL in .env.production." -ForegroundColor Green
