# run_importease_secure.ps1
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "==============================================="
Write-Host " ImportEase Secure Runner (PowerShell)"
Write-Host "==============================================="

$env:JAVA_HOME = "C:\Program Files\Apache NetBeans\jdk"
$MavenCmd = "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd"

if (-not (Test-Path $MavenCmd)) {
    Write-Error "[ERROR] No se encontro Maven de NetBeans en: $MavenCmd"
    exit 1
}

if (Test-Path .env) {
    Write-Host "[INFO] Cargando variables de entorno desde .env..."
    Get-Content .env | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            if ($line -match '^([^=]+)=(.*)$') {
                $key = $Matches[1].Trim()
                $value = $Matches[2].Trim()
                if ($value -match '^"(.*)"$') { $value = $Matches[1] }
                elseif ($value -match "^'(.*)'$") { $value = $Matches[1] }
                
                # Only set environment variable if value is not empty to avoid wiping out system-defined variables
                if ($value -ne "") {
                    [System.Environment]::SetEnvironmentVariable($key, $value, [System.EnvironmentVariableTarget]::Process)
                }
            }
        }
    }
}

Write-Host "[INFO] Compilando y levantando la aplicacion..."
Write-Host "[INFO] Utilizando variables cargadas de .env"

# Run Spring Boot in-process using Maven
& $MavenCmd -DskipTests spring-boot:run
