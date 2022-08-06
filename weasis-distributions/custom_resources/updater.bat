@echo off
@setlocal enableextensions
@cd /d "%~dp0"


:: working
 IF EXIST "updater_new.exe" (
    move "C:\Program Files\MedReport360-Viewer\app\updater_new.exe" "C:\Program Files\MedReport360-Viewer\app\updater.exe"
    echo "updated the updater today" - %date% - %time% >>"C:\Program Files\MedReport360-Viewer\app\logs.txt"
) 



