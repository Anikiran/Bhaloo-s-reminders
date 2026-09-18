@echo off
REM Same as RENDER-GLASS.bat but only two buttons at low quality,
REM so you can check it works in a few seconds before the full run.
setlocal enabledelayedexpansion
title Bhaloo Glass Kit - quick preview
color 0E
echo.
echo   QUICK PREVIEW - 2 buttons, low quality, just to check it works.
echo.
set "BLENDER="
if not "%~1"=="" if /i "%~nx1"=="blender.exe" set "BLENDER=%~1"
if not defined BLENDER if exist "%~dp0blender.exe"        set "BLENDER=%~dp0blender.exe"
if not defined BLENDER if exist "%~dp0blender\blender.exe" set "BLENDER=%~dp0blender\blender.exe"
if not defined BLENDER for %%P in (
    "D:\blender\blender.exe" "D:\Blender\blender.exe"
    "D:\Blender Portable\blender.exe" "D:\BlenderPortable\blender.exe"
    "D:\Program Files\Blender Foundation\Blender\blender.exe"
) do (if not defined BLENDER if exist %%P set "BLENDER=%%~P")
if not defined BLENDER (
    echo  Could not find blender.exe - drag it onto this file.
    pause & exit /b 1
)
set "OUT=%~dp0renders"
if not exist "!OUT!" mkdir "!OUT!"
"!BLENDER!" -b -P "%~dp0glass_kit.py" -- --out "!OUT!" --samples 48 --only btn_primary,btn_secondary
if not errorlevel 1 start "" "!OUT!"
pause
