@echo off
chcp 65001 >nul
echo TESTANDO CAMERAS DISPONIVEIS...
echo.

echo Dispositivos de camera detectados pelo Windows:
powershell -Command "Get-PnpDevice | Where-Object {$_.FriendlyName -like '*camera*' -or $_.FriendlyName -like '*webcam*' -or $_.Class -eq 'Camera'} | Format-Table FriendlyName, Status"

echo.
echo Processos que podem estar usando camera:
tasklist | findstr /i "camera teams skype discord chrome firefox edge obs zoom msedgewebview2"

echo.
echo Se houver processos acima, execute: liberar-camera.ps1
echo Depois execute: run-with-camera.bat
pause