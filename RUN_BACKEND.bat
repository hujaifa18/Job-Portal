@echo off
REM ============================================================
REM  Job Portal Backend — Quick Start Script (Fixed)
REM  Requirements: Java 8+, XAMPP with MySQL running
REM ============================================================

echo.
echo  ============================================
echo   Job Portal Backend
echo  ============================================
echo.

REM Check Java is installed
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo  [ERROR] Java not found. Install Java 8+ and add it to PATH.
    pause
    exit /b 1
)

REM Navigate to project root
cd /d "%~dp0"

echo  [1/2] Compiling Java source files...
echo.

REM Compile all source files
javac -d backend\bin -cp "backend\lib\*;backend\src" ^
    backend\src\util\DBConnection.java ^
    backend\src\model\*.java ^
    backend\src\dao\*.java ^
    backend\src\service\*.java ^
    backend\src\server\Server.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo  [ERROR] Compilation failed. Check errors above.
    pause
    exit /b 1
)

echo.
echo  [OK] Compilation successful!
echo.
echo  [2/2] Starting server on http://localhost:8080
echo.
echo  Make sure XAMPP MySQL is running before this step!
echo  Press Ctrl+C to stop the server.
echo.

REM Create uploads directory if missing
if not exist "uploads" mkdir uploads

REM Run server
java -cp "backend\bin;backend\lib\*" server.Server

pause
