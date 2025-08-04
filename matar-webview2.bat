@echo off
echo MATANDO TODOS OS PROCESSOS WEBVIEW2...
taskkill /f /im "msedgewebview2.exe" 2>nul

echo Aguardando 2 segundos...
timeout /t 2 /nobreak >nul

echo Verificando se ainda existem processos WebView2...
tasklist | findstr /i "msedgewebview2" >nul
if %ERRORLEVEL% EQU 0 (
    echo AINDA EXISTEM PROCESSOS WEBVIEW2 - Tentando novamente...
    taskkill /f /im "msedgewebview2.exe" 2>nul
    timeout /t 1 /nobreak >nul
) else (
    echo SUCESSO! Todos os processos WebView2 foram finalizados.
)

echo.
echo Execute agora: run-with-camera.bat
pause