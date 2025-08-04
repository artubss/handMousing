@echo off
chcp 65001 >nul
echo LIBERANDO CAMERA FORCADAMENTE...
echo.

echo Parando aplicativo Camera do Windows...
taskkill /f /im "Camera.exe" 2>nul
taskkill /f /im "WindowsCamera.exe" 2>nul

echo Parando programas de video...
taskkill /f /im "SkypeApp.exe" 2>nul
taskkill /f /im "Teams.exe" 2>nul
taskkill /f /im "Discord.exe" 2>nul
taskkill /f /im "obs64.exe" 2>nul
taskkill /f /im "Zoom.exe" 2>nul

echo Parando navegadores e WebView2...
taskkill /f /im "chrome.exe" 2>nul
taskkill /f /im "firefox.exe" 2>nul
taskkill /f /im "msedge.exe" 2>nul
taskkill /f /im "msedgewebview2.exe" 2>nul

echo Reiniciando servicos de camera do Windows...
net stop "Windows Camera Frame Server" 2>nul
timeout /t 3 /nobreak >nul
net start "Windows Camera Frame Server" 2>nul

echo Aguardando liberacao da camera...
timeout /t 2 /nobreak >nul

echo.
echo SUCESSO! Camera liberada! Execute agora: run-with-camera.bat
pause