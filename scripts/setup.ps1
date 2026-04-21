param(
    [string]$DbUser = $env:QUIZ_DB_USER,
    [string]$DbPass = $env:QUIZ_DB_PASS,
    [string]$DbUrl = $env:QUIZ_DB_URL
)

$ErrorActionPreference = "Stop"

if (-not $DbUser) { $DbUser = "root" }
if (-not $DbUrl) {
    $DbUrl = "jdbc:mysql://localhost:3306/survey?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
}

$env:QUIZ_DB_USER = $DbUser
$env:QUIZ_DB_PASS = $DbPass
$env:QUIZ_DB_URL = $DbUrl

Write-Host "[1/3] Checking Java tools..."
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    throw "javac not found. Install JDK 8+ and add it to PATH."
}

Write-Host "[2/3] Applying database schema and seed data (if mysql is available)..."
if (Get-Command mysql -ErrorAction SilentlyContinue) {
    if ($DbPass) { $env:MYSQL_PWD = $DbPass }
    Get-Content "database/schema_seed.sql" | mysql -u $DbUser
    if ($DbPass) { Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue }
    Write-Host "Database setup complete."
} else {
    Write-Warning "mysql client not found. Run database/schema_seed.sql manually in MySQL."
}

Write-Host "[3/3] Compiling Java sources..."
if (-not (Test-Path "out")) { New-Item -ItemType Directory -Path "out" | Out-Null }
javac -d out src\*.java

Write-Host ""
Write-Host "Setup complete."
Write-Host "Use this helper to compile and run with DB settings:"
Write-Host './scripts/run.ps1 -ConnectorJar "C:\path\to\mysql-connector-j-8.x.x.jar" -DbUser root -DbPass your_password'
Write-Host 'Or prompt securely for the password:'
Write-Host './scripts/run.ps1 -ConnectorJar "C:\path\to\mysql-connector-j-8.x.x.jar" -DbUser root -PromptForDbPass'
