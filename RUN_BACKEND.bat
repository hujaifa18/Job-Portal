@echo off
REM Job Portal Backend - Quick Start Script

echo.
echo ========================================
echo Job Portal Backend - Starting...
echo ========================================
echo.

REM Change to backend directory
cd /d "%~dp0backend"

echo.
echo [1/2] Compiling Java files...
echo.

REM Compile
javac -d bin -cp "lib/*:src" src/util/DBConnection.java src/model/*.java src/dao/*.java src/service/*.java src/server/Server.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Compilation failed!
    echo Check the errors above.
    pause
    exit /b 1
)

echo.
echo ✅ Compilation successful!
echo.
echo [2/2] Starting Java Server on port 8080...
echo.

REM Run
java -cp "bin;lib/*" server.Server

pause
