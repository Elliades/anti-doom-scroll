# Free port 8080 (e.g. after "Port 8080 was already in use" from ./gradlew bootRun).
# Usage: .\scripts\free-port-8080.ps1           # list process using 8080
#        .\scripts\free-port-8080.ps1 -Kill     # list and kill it

param([switch]$Kill)

$port = 8080
$conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
if (-not $conn) {
    Write-Host "No process is listening on port $port." -ForegroundColor Green
    exit 0
}

$pids = $conn.OwningProcess | Sort-Object -Unique
foreach ($procId in $pids) {
    $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
    $name = if ($proc) { $proc.ProcessName } else { "PID $procId" }
    Write-Host "Port $port is in use by: $name (PID $procId)" -ForegroundColor Yellow
    if ($Kill) {
        Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
        Write-Host "  -> Stopped PID $procId" -ForegroundColor Green
    }
}

if (-not $Kill -and $pids) {
    Write-Host "To stop the process(es), run: .\scripts\free-port-8080.ps1 -Kill" -ForegroundColor Cyan
}
