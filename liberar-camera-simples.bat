@echo off
echo LIBERANDO CAMERA - METODO SIMPLES...
echo.

echo Fechando app Camera nativo do Windows...
taskkill /f /im "WindowsCamera.exe" 2>nul

echo Testando se camera funciona no app nativo...
echo (Vai abrir o app Camera - se funcionar, feche e continue)
start ms-windows-store://pdp/?productid=9WZDNCRFJBBG
timeout /t 8 /nobreak >nul

echo Fechando app Camera...
taskkill /f /im "WindowsCamera.exe" 2>nul

echo Aguardando camera ser liberada...
timeout /t 3 /nobreak >nul

echo.
echo SE A CAMERA FUNCIONOU NO APP NATIVO:
echo Execute agora: .\run-with-camera.bat
echo.
echo SE NAO FUNCIONOU:
echo 1. Execute PowerShell como Administrador
echo 2. Execute: .\corrigir-camera.ps1
echo 3. Reinicie o computador
pause