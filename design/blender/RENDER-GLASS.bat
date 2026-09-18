@echo off
setlocal enabledelayedexpansion
title Bhaloo Glass Kit - rendering
color 0B

echo.
echo  ============================================================
echo    Bhaloo Glass Kit  -  Blender render
echo  ============================================================
echo.
echo  You do NOT need to open Blender. This does everything.
echo.

REM ---------------------------------------------------------------
REM  1. Find blender.exe
REM ---------------------------------------------------------------
set "BLENDER="

REM  (a) dragged onto this file?
if not "%~1"=="" (
    if /i "%~nx1"=="blender.exe" set "BLENDER=%~1"
)

REM  (b) sitting next to this file, or one folder down
if not defined BLENDER if exist "%~dp0blender.exe"           set "BLENDER=%~dp0blender.exe"
if not defined BLENDER if exist "%~dp0blender\blender.exe"    set "BLENDER=%~dp0blender\blender.exe"

REM  (c) the usual portable spots on D:
if not defined BLENDER for %%P in (
    "D:\blender\blender.exe"
    "D:\Blender\blender.exe"
    "D:\Blender Portable\blender.exe"
    "D:\BlenderPortable\blender.exe"
    "D:\Program Files\Blender Foundation\Blender\blender.exe"
    "D:\Programs\Blender\blender.exe"
    "D:\Apps\Blender\blender.exe"
) do (
    if not defined BLENDER if exist %%P set "BLENDER=%%~P"
)

REM  (d) last resort: search D: for it
if not defined BLENDER (
    echo  Looking for blender.exe on D: ... this can take a minute.
    for /f "delims=" %%F in ('dir /b /s "D:\blender.exe" 2^>nul') do (
        if not defined BLENDER set "BLENDER=%%F"
    )
)

if not defined BLENDER (
    echo.
    echo  ---------------------------------------------------------
    echo   Could not find blender.exe automatically.
    echo.
    echo   EASY FIX: drag your blender.exe file and drop it
    echo   onto this RENDER-GLASS.bat file. That's it.
    echo  ---------------------------------------------------------
    echo.
    pause
    exit /b 1
)

echo  Using Blender:
echo    !BLENDER!
echo.

REM ---------------------------------------------------------------
REM  2. Render
REM ---------------------------------------------------------------
set "OUT=%~dp0renders"
if not exist "!OUT!" mkdir "!OUT!"

echo  Rendering into:
echo    !OUT!
echo.
echo  This window will show progress. Leave it open.
echo.

"!BLENDER!" -b -P "%~dp0glass_kit.py" -- --out "!OUT!" --samples 180

echo.
if errorlevel 1 (
    echo  ---------------------------------------------------------
    echo   Something went wrong. Copy the red text above and
    echo   send it to Claude - it will say exactly what to fix.
    echo  ---------------------------------------------------------
) else (
    echo  ============================================================
    echo    Done. Opening the folder with your PNG files.
    echo    Send those PNGs back to Claude.
    echo  ============================================================
    start "" "!OUT!"
)
echo.
pause
