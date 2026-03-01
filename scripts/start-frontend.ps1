# Start frontend dev server (Vite). Proxies /api to backend on 5173.
# Usage: .\scripts\start-frontend.ps1

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptDir

Set-Location "$projectRoot\frontend"
if (-not (Test-Path "node_modules")) {
    Write-Host "Running npm install..." -ForegroundColor Cyan
    npm install
}
Write-Host "Starting frontend (Vite)..." -ForegroundColor Cyan
Write-Host "Open http://localhost:5174 (or next available port if 5174 is in use)" -ForegroundColor Green
npm run dev
