param(
    [string]$ConnectorJar,
    [string]$DbUser = $env:QUIZ_DB_USER,
    [string]$DbPass = $env:QUIZ_DB_PASS,
    [string]$DbUrl = $env:QUIZ_DB_URL,
    [switch]$PromptForDbPass
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $repoRoot

if (-not $DbUser) { $DbUser = "root" }
if (-not $DbUrl) {
    $DbUrl = "jdbc:mysql://localhost:3306/survey?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
}

if ($PromptForDbPass -and -not $DbPass) {
    $securePassword = Read-Host "MySQL password" -AsSecureString
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try {
        $DbPass = [Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    }
}

if (-not $ConnectorJar) {
    $searchRoots = @($repoRoot)
    $parentRoot = Split-Path $repoRoot -Parent
    if ($parentRoot) {
        $searchRoots += $parentRoot
    }

    $detectedJar = Get-ChildItem -Path $searchRoots -Filter "mysql-connector-j-*.jar" -File -Recurse -ErrorAction SilentlyContinue |
        Select-Object -First 1

    if ($detectedJar) {
        $ConnectorJar = $detectedJar.FullName
    }
}

if (-not $ConnectorJar) {
    throw "MySQL Connector/J JAR not found. Pass -ConnectorJar 'C:\path\to\mysql-connector-j-x.y.z.jar'."
}

$ConnectorJar = (Resolve-Path $ConnectorJar).Path

$env:QUIZ_DB_USER = $DbUser
$env:QUIZ_DB_PASS = $DbPass
$env:QUIZ_DB_URL = $DbUrl

Write-Host "[1/2] Compiling Java sources..."
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    throw "javac not found. Install JDK 8+ and add it to PATH."
}

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw "java not found. Install JDK 8+ and add it to PATH."
}

if (-not (Test-Path "out")) {
    New-Item -ItemType Directory -Path "out" | Out-Null
}

javac -d out src\*.java

Write-Host "[2/2] Launching Quiz Studio..."
$classpath = "out;$ConnectorJar"
& java -cp $classpath runner