@echo off
echo ==========================================
echo   CORRIGINDO ACESSO PROGRAMATICO A CAMERA
echo ==========================================
echo.

echo STEP 1: Habilitando Frame Server Mode...
reg add "HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows Media Foundation\Platform" /v EnableFrameServerMode /t REG_DWORD /d 1 /f
reg add "HKEY_LOCAL_MACHINE\SOFTWARE\WOW6432Node\Microsoft\Windows Media Foundation\Platform" /v EnableFrameServerMode /t REG_DWORD /d 1 /f

echo STEP 2: Configurando permissoes de camera para aplicacoes desktop...
reg add "HKEY_CURRENT_USER\SOFTWARE\Microsoft\Windows\CurrentVersion\CapabilityAccessManager\ConsentStore\webcam" /v Value /t REG_SZ /d Allow /f
reg add "HKEY_CURRENT_USER\SOFTWARE\Microsoft\Windows\CurrentVersion\CapabilityAccessManager\ConsentStore\webcam\NonPackaged" /v Value /t REG_SZ /d Allow /f

echo STEP 3: Habilitando acesso para aplicacoes Java/Desktop...
reg add "HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\CapabilityAccessManager\ConsentStore\webcam" /v Value /t REG_SZ /d Allow /f
reg add "HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\CapabilityAccessManager\ConsentStore\webcam\NonPackaged" /v Value /t REG_SZ /d Allow /f

echo STEP 4: Configurando politicas de camera...
reg add "HKEY_LOCAL_MACHINE\SOFTWARE\Policies\Microsoft\Windows\AppPrivacy" /v LetAppsAccessCamera /t REG_DWORD /d 1 /f
reg add "HKEY_LOCAL_MACHINE\SOFTWARE\Policies\Microsoft\Windows\AppPrivacy" /v LetAppsAccessCamera_UserInControlOfTheseApps /t REG_MULTI_SZ /d "java.exe\0javaw.exe\0" /f

echo STEP 5: Reiniciando servicos necessarios...
net stop FrameServer 2>nul
timeout /t 2 /nobreak >nul
net start FrameServer 2>nul

echo STEP 6: Limpando cache de aplicacoes...
taskkill /f /im "ApplicationFrameHost.exe" 2>nul

echo.
echo ==========================================
echo   ACESSO CORRIGIDO! REINICIE O COMPUTADOR
echo   Depois execute: .\run-with-camera.bat
echo ==========================================
echo.
echo IMPORTANTE: Execute este script como ADMINISTRADOR!
pause