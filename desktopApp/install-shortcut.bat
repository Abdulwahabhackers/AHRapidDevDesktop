@echo off
title AH RapidDev - Install Shortcut

echo.
echo ========================================
echo   AH RapidDev - Desktop Shortcut
echo ========================================
echo.

set "APP_DIR=%~dp0"
set "APP_EXE=%APP_DIR%AH RapidDev.exe"

if not exist "%APP_EXE%" (
    echo [ERROR] Could not find AH RapidDev.exe
    pause
    exit /b 1
)

echo [OK] Found application
echo.

powershell -NoProfile -ExecutionPolicy Bypass -Command "=[Environment]::GetFolderPath('Desktop'); =(New-Object -ComObject WScript.Shell).CreateShortcut(\"\AH RapidDev.lnk\"); .TargetPath='%APP_EXE%'; .WorkingDirectory='%APP_DIR%'; .Description='AH RapidDev - Desktop Edition'; .IconLocation='%APP_EXE%,0'; .Save()"

if errorlevel 1 (
    echo [ERROR] Failed to create shortcut
    pause
    exit /b 1
)

echo [OK] Desktop shortcut created!
echo.
echo ========================================
echo   Complete!
echo ========================================
echo.
pause