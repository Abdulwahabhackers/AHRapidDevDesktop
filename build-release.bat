@echo off
title AH RapidDev - Build Script

cd /d "%~dp0"

echo.
echo ========================================
echo   AH RapidDev - Full Build
echo ========================================
echo.

if defined JAVA_HOME goto java_ok

echo [Setup] Locating JDK...

if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
    goto found_java
)
if exist "C:\Program Files\Android\Android Studio\jre\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Android\Android Studio\jre"
    goto found_java
)

for /d %%P in ("C:\Program Files\Java\jdk-*") do (
    if exist "%%~P\bin\java.exe" (
        set "JAVA_HOME=%%~P"
        goto found_java
    )
)
for /d %%P in ("C:\Program Files\Eclipse Adoptium\jdk-*") do (
    if exist "%%~P\bin\java.exe" (
        set "JAVA_HOME=%%~P"
        goto found_java
    )
)

echo [ERROR] Java not found. Please install JDK 17.
pause
exit /b 1

:found_java
echo [OK] Found Java at: %JAVA_HOME%

:java_ok
echo Using JAVA_HOME: %JAVA_HOME%
echo.

echo [1/4] Building application...
call gradlew.bat :desktopApp:createDistributable
if errorlevel 1 (
    echo [ERROR] Build failed
    pause
    exit /b 1
)
echo [OK] Build complete
echo.

echo [2/4] Copying icon.ico...

set "BUILD_DIR=desktopApp\build\compose\binaries\main\app\AH RapidDev"

if not exist "%BUILD_DIR%" (
    echo [ERROR] Build directory not found
    pause
    exit /b 1
)

if exist "desktopApp\icon.ico" (
    copy /Y "desktopApp\icon.ico" "%BUILD_DIR%\icon.ico" >nul
    echo [OK] icon.ico copied
) else (
    echo [WARNING] icon.ico not found - shortcut will use default icon
)
echo.

echo [3/4] Copying shortcut installer...
copy /Y "desktopApp\install-shortcut.bat" "%BUILD_DIR%\install-shortcut.bat" >nul
echo [OK] Copied
echo.

echo [4/4] Done!
echo.
echo ========================================
echo   Build successful!
echo ========================================
echo.
echo Location:
echo   %BUILD_DIR%
echo.
pause