@echo off
setlocal EnableExtensions EnableDelayedExpansion

cd /d "%~dp0"

echo ===============================================
echo  ImportEase Secure Runner
echo ===============================================

set "JAVA_HOME=C:\Program Files\Apache NetBeans\jdk"
set "MAVEN_CMD=C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd"

if not exist "%MAVEN_CMD%" (
  echo [ERROR] No se encontro Maven de NetBeans en:
  echo         %MAVEN_CMD%
  exit /b 1
)

echo [INFO] Iniciando el servidor a traves de PowerShell...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run_importease_secure.ps1"
set "EXIT_CODE=%ERRORLEVEL%"

echo.
if not "%EXIT_CODE%"=="0" (
  echo [ERROR] La ejecucion termino con codigo %EXIT_CODE%.
) else (
  echo [OK] App ejecutada correctamente.
)

exit /b %EXIT_CODE%
