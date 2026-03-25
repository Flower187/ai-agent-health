@echo off
chcp 65001 >nul
echo ========================================
echo AI Fitness Master - Installing
echo ========================================
echo.

cd /d %~dp0

if not exist node_modules (
    echo Installing dependencies, please wait...
    call npm install --registry=https://registry.npmmirror.com
    if errorlevel 1 (
        echo.
        echo Installation failed! Please check Node.js and npm
        pause
        exit /b 1
    )
    echo.
    echo Installation complete!
) else (
    echo Dependencies already exist, skip installation
)

echo.
echo ========================================
echo Starting Development Server
echo ========================================
echo.
echo Access URL: http://localhost:3000
echo Backend API: http://localhost:8123/api
echo.

call npm run dev

pause
