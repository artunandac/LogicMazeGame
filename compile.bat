@echo off
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -cp lib\Enigma-Edited2.jar -d out src\*.java
if %ERRORLEVEL% == 0 (
    echo.
    echo === COMPILE OK ===
) else (
    echo.
    echo === COMPILE FAILED ===
)
pause
