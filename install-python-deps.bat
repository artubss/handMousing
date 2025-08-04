@echo off
echo 🐍 Instalando dependências Python para detecção de mãos...

REM Verifica se Python está instalado
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Python não encontrado! Instale Python 3.8+ primeiro.
    echo 💡 Baixe em: https://www.python.org/downloads/
    pause
    exit /b 1
)

echo ✅ Python encontrado
echo 📦 Instalando dependências...

REM Instala as dependências
pip install -r requirements.txt

if errorlevel 1 (
    echo ❌ Erro ao instalar dependências!
    pause
    exit /b 1
)

echo ✅ Dependências instaladas com sucesso!
echo 🚀 Agora você pode executar: python hand_detection_service.py
pause 