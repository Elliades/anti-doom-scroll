# Start backend with local profile (H2, no Docker/PostgreSQL).
# Frees port 5173 first if in use, then runs bootRun.

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptDir

# Free port 5173 (stops any existing backend instance)
& "$scriptDir\free-port-5173.ps1" -Kill

# Start backend (clean build ensures stale Gradle cache doesn't serve old classes)
Set-Location $projectRoot
.\gradlew.bat clean bootRun --args='--spring.profiles.active=local'
