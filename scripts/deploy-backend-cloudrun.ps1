# Deploy backend to Cloud Run: sets DB password on the service, then builds and deploys the image.
#
# From project root:
#   $env:NEON_DB_PASSWORD = "your-neon-password"
#   .\scripts\deploy-backend-cloudrun.ps1
#
# If gcloud is not installed, run once to install it (Windows):
#   .\scripts\deploy-backend-cloudrun.ps1 -InstallGcloud
# Then close and reopen PowerShell, set NEON_DB_PASSWORD, and run the script again.

param(
    [string] $ServiceName = "anti-doom-scroll",
    [string] $Region = "europe-west1",
    [string] $ProjectId = "",
    [switch] $InstallGcloud
)

$ErrorActionPreference = "Stop"
$scriptDir = $PSScriptRoot
$projectRoot = Split-Path -Parent $scriptDir
Set-Location $projectRoot

# Resolve gcloud: env override, then PATH, then common Windows install locations, then search
$gcloudExe = $null
if ($env:GCLOUD_PATH -and (Test-Path -LiteralPath $env:GCLOUD_PATH)) { $gcloudExe = $env:GCLOUD_PATH }
if (-not $gcloudExe) {
    $gcloudCmd = Get-Command "gcloud" -ErrorAction SilentlyContinue
    if ($gcloudCmd) { $gcloudExe = $gcloudCmd.Source }
}
if (-not $gcloudExe) {
    $candidates = @(
        "$env:LOCALAPPDATA\Google\Cloud SDK\google-cloud-sdk\bin\gcloud.cmd",
        "${env:ProgramFiles(x86)}\Google\Cloud SDK\google-cloud-sdk\bin\gcloud.cmd",
        "$env:ProgramFiles\Google\Cloud SDK\google-cloud-sdk\bin\gcloud.cmd",
        "$env:USERPROFILE\Google\Cloud SDK\google-cloud-sdk\bin\gcloud.cmd"
    )
    foreach ($p in $candidates) {
        if ($p -and (Test-Path -LiteralPath $p)) { $gcloudExe = $p; break }
    }
}
if (-not $gcloudExe -and $env:LOCALAPPDATA) {
    $googleBin = Get-ChildItem -Path "$env:LOCALAPPDATA\Google" -Filter "gcloud.cmd" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($googleBin) { $gcloudExe = $googleBin.FullName }
}
if (-not $gcloudExe) {
    Write-Host "gcloud CLI not found." -ForegroundColor Red
    Write-Host "Install: https://cloud.google.com/sdk/docs/install" -ForegroundColor Yellow
    Write-Host "If already installed, add its 'bin' folder to your PATH, or run this from a terminal where 'gcloud' works." -ForegroundColor Yellow
    exit 1
}

function Invoke-Gcloud {
    & $gcloudExe @args
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

$password = $env:NEON_DB_PASSWORD
if (-not $password) {
    Write-Host "Set NEON_DB_PASSWORD first. Example: `$env:NEON_DB_PASSWORD = 'your-password'" -ForegroundColor Red
    exit 1
}

$projectArg = @(); if ($ProjectId) { $projectArg = @("--project=$ProjectId") }

Write-Host "Setting SPRING_DATASOURCE_PASSWORD on service $ServiceName..." -ForegroundColor Cyan
Invoke-Gcloud run services update $ServiceName --region $Region --set-env-vars "SPRING_DATASOURCE_PASSWORD=$password" @projectArg

Write-Host "Building and deploying image (this may take a few minutes)..." -ForegroundColor Cyan
Invoke-Gcloud run deploy $ServiceName --source . --region $Region --platform managed --allow-unauthenticated @projectArg

Write-Host "Done. Backend is live and connected to Neon." -ForegroundColor Green
