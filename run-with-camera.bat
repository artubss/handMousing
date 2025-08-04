@echo off
echo 🎥 TouchVirtual - Iniciando com Câmera Real
echo.

REM Libera a camera de outros processos AGRESSIVAMENTE
echo LIBERANDO CAMERA DE TODOS OS PROCESSOS...
taskkill /f /im "Camera.exe" 2>nul
taskkill /f /im "WindowsCamera.exe" 2>nul
taskkill /f /im "SkypeApp.exe" 2>nul
taskkill /f /im "Teams.exe" 2>nul
taskkill /f /im "Discord.exe" 2>nul
taskkill /f /im "chrome.exe" 2>nul
taskkill /f /im "firefox.exe" 2>nul
taskkill /f /im "msedge.exe" 2>nul
taskkill /f /im "msedgewebview2.exe" 2>nul
taskkill /f /im "obs64.exe" 2>nul
taskkill /f /im "Zoom.exe" 2>nul

echo Reiniciando servicos de camera...
net stop "Windows Camera Frame Server" 2>nul
timeout /t 3 /nobreak >nul
net start "Windows Camera Frame Server" 2>nul

echo Aguardando liberacao da camera...
timeout /t 2 /nobreak >nul

echo.
echo 🚀 Iniciando TouchVirtual com configurações otimizadas...
echo 💡 Robot: Habilitado (modo GUI)
echo 🎥 Câmera: Real (não simulada)
echo.

REM Executa com todas as configurações necessárias
.\mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.awt.headless=false -Dfile.encoding=UTF-8 -Xmx2048m -Djava.library.path=target/classes"

echo.
echo ✅ TouchVirtual finalizado
pause