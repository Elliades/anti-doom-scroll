# Set the DB password on the Cloud Run service. All other prod config is in application-prod.yml (in the image).
# Run once after the first deploy, or when you rotate the Neon password.
#
# Usage: $env:NEON_DB_PASSWORD = "your-password" ; .\scripts\set-production-env-cloudrun.ps1

param(
    [string] $ServiceName = "anti-doom-scroll",
    [string] $Region = "europe-west1",
    [string] $ProjectId = ""
)

$ErrorActionPreference = "Stop"
$scriptDir = $PSScriptRoot
$projectRoot = Split-Path -Parent $scriptDir
Set-Location $projectRoot

$password = $env:NEON_DB_PASSWORD
if (-not $password) {
    Write-Error "Set NEON_DB_PASSWORD: `$env:NEON_DB_PASSWORD = 'your-password'"
    exit 1
}

if (-not (Get-Command "gcloud" -ErrorAction SilentlyContinue)) {
    Write-Error "gcloud CLI not found."
    exit 1
}

$projectArg = if ($ProjectId) { "--project=$ProjectId" } else { "" }

Write-Host "Setting SPRING_DATASOURCE_PASSWORD on Cloud Run service: $ServiceName..." -ForegroundColor Cyan
gcloud run services update $ServiceName `
    --region $Region `
    --set-env-vars "SPRING_DATASOURCE_PASSWORD=$password" `
    $projectArg

if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "Done. Backend uses application-prod.yml (Neon + CORS for Firebase)." -ForegroundColor Green
