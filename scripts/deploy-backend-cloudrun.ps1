# Deploy backend to Google Cloud Run (builds from Dockerfile at repo root).
# Image uses application-prod.yml (Neon + CORS). Set SPRING_DATASOURCE_PASSWORD in Cloud Run (once) via set-production-env-cloudrun.ps1.
#
# Usage: .\scripts\deploy-backend-cloudrun.ps1

param(
    [string] $ServiceName = "anti-doom-scroll",
    [string] $Region = "europe-west1",
    [string] $ProjectId = ""  # empty = use gcloud default
)

$ErrorActionPreference = "Stop"
$scriptDir = $PSScriptRoot
$projectRoot = Split-Path -Parent $scriptDir
Set-Location $projectRoot

if (-not (Get-Command "gcloud" -ErrorAction SilentlyContinue)) {
    Write-Error "gcloud CLI not found. Install: https://cloud.google.com/sdk/docs/install"
    exit 1
}

$projectArg = if ($ProjectId) { "--project=$ProjectId" } else { "" }

Write-Host "Deploying backend to Cloud Run (service: $ServiceName, region: $Region)..." -ForegroundColor Cyan
Write-Host "Building from Dockerfile and deploying (this may take a few minutes)..." -ForegroundColor Gray

# --source . builds the image in Cloud Build from the Dockerfile, then deploys.
# Cloud Run sets PORT=8080; our app uses server.port=${PORT:5173} so it listens on 8080.
gcloud run deploy $ServiceName `
    --source . `
    --region $Region `
    --platform managed `
    --allow-unauthenticated `
    $projectArg

if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host ""
Write-Host "Done. Set SPRING_DATASOURCE_PASSWORD once: `$env:NEON_DB_PASSWORD='...'; .\scripts\set-production-env-cloudrun.ps1" -ForegroundColor Yellow
