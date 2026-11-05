param(
    [switch]$ConfirmDemoSeed
)

$ErrorActionPreference = 'Stop'

if (-not $ConfirmDemoSeed) {
    throw 'This optional catalog seed requires -ConfirmDemoSeed. It is for disposable local Compose databases only.'
}

$seedFile = Join-Path (Get-Location) 'scripts/demo-seed.sql'
if (-not (Test-Path -LiteralPath $seedFile)) {
    throw 'Run this script from the EventHub repository root.'
}

$postgresContainer = docker compose ps --status running -q postgres
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($postgresContainer)) {
    throw 'The local Compose postgres service is not running.'
}

$database = (docker compose exec -T postgres printenv POSTGRES_DB).Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($database)) { throw 'Could not read the local database name from the Compose postgres service.' }
$dbUser = (docker compose exec -T postgres printenv POSTGRES_USER).Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($dbUser)) { throw 'Could not read the local database user from the Compose postgres service.' }

$confirmation = Read-Host "This inserts fictional EventHub events and seats into local Compose database '$database'. Type SEED LOCAL DEMO CATALOG to proceed"
if ($confirmation -cne 'SEED LOCAL DEMO CATALOG') {
    Write-Host 'Demo seed cancelled.'
    exit 0
}

docker compose cp $seedFile postgres:/tmp/demo-seed.sql
if ($LASTEXITCODE -ne 0) { throw 'Could not copy the demo seed into the local PostgreSQL container.' }
docker compose exec -T postgres psql -v ON_ERROR_STOP=1 -U $dbUser -d $database -f /tmp/demo-seed.sql
if ($LASTEXITCODE -ne 0) { throw 'The optional demo catalog seed did not complete.' }
