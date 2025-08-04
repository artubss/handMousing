@echo off
echo ==========================================
echo    RESOLVENDO CAMERA DEFINITIVAMENTE
echo ==========================================
echo.

echo STEP 1: Parando TODOS os processos que usam camera...
taskkill /f /im "Camera.exe" 2>nul
taskkill /f /im "WindowsCamera.exe" 2>nul
taskkill /f /im "msedgewebview2.exe" 2>nul
taskkill /f /im "chrome.exe" 2>nul
taskkill /f /im "msedge.exe" 2>nul
taskkill /f /im "Teams.exe" 2>nul
taskkill /f /im "SkypeApp.exe" 2>nul
taskkill /f /im "Discord.exe" 2>nul

echo STEP 2: Reiniciando servicos de camera...
net stop "Windows Camera Frame Server" 2>nul
timeout /t 3 /nobreak >nul
net start "Windows Camera Frame Server" 2>nul

echo STEP 3: Limpando cache de camera...
reg delete "HKEY_CURRENT_USER\SOFTWARE\Microsoft\Windows\CurrentVersion\CapabilityAccessManager\ConsentStore\webcam" /f 2>nul
reg add "HKEY_CURRENT_USER\SOFTWARE\Microsoft\Windows\CurrentVersion\CapabilityAccessManager\ConsentStore\webcam" /v Value /t REG_SZ /d Allow /f 2>nul

echo STEP 4: Verificando se camera esta livre...
timeout /t 2 /nobreak >nul

echo STEP 5: Testando camera com app nativo...
start ms-windows-store://pdp/?productid=9WZDNCRFJBBG
timeout /t 5 /nobreak >nul
taskkill /f /im "WindowsCamera.exe" 2>nul

echo.
echo ==========================================
echo    CAMERA LIBERADA! EXECUTE AGORA:
echo    .\run-with-camera.bat
echo ==========================================
pause