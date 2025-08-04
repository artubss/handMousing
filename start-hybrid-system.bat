@echo off
echo 🚀 Iniciando Sistema Híbrido TouchVirtual (Java + Python)
echo.

REM Verifica se Python está instalado
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Python não encontrado! Instale Python 3.8+ primeiro.
    echo 💡 Baixe em: https://www.python.org/downloads/
    pause
    exit /b 1
)

echo ✅ Python encontrado
echo.

REM Verifica se as dependências Python estão instaladas
echo 📦 Verificando dependências Python...
python -c "import cv2, mediapipe, flask, requests" >nul 2>&1
if errorlevel 1 (
    echo ❌ Dependências Python não encontradas!
    echo 💡 Execute: install-python-deps.bat
    pause
    exit /b 1
)

echo ✅ Dependências Python OK
echo.

REM Inicia o serviço Python em background
echo 🐍 Iniciando serviço Python de detecção...
start /B python hand_detection_service.py

REM Aguarda um pouco para o Python inicializar
timeout /t 3 /nobreak >nul

REM Verifica se o Python está rodando
echo 🔍 Verificando se o serviço Python está rodando...
curl -s http://localhost:5000/api/hand-detection/health >nul 2>&1
if errorlevel 1 (
    echo ❌ Serviço Python não está respondendo!
    echo 💡 Verifique se a porta 5000 está livre
    pause
    exit /b 1
)

echo ✅ Serviço Python OK
echo.

REM Inicia o sistema Java
echo ☕ Iniciando sistema Java...
echo.

REM Executa o sistema Java
call mvn spring-boot:run

echo.
echo 🛑 Sistema parado
pause 