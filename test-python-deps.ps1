Write-Host "Testando dependencias Python..." -ForegroundColor Green

# Testa diferentes versões do Python
$pythonVersions = @("3.11", "3.13")

foreach ($version in $pythonVersions) {
    Write-Host "`nTestando Python $version..." -ForegroundColor Cyan
    
    try {
        # Testa se Python está disponível
        $pythonVersion = & "py" "-$version" "--version" 2>&1
        Write-Host "Python $version encontrado: $pythonVersion" -ForegroundColor Green
        
        # Testa importação das dependências
        $testScript = @'
import sys
print("Python " + sys.version)

try:
    import numpy as np
    print("NumPy: " + np.__version__)
except ImportError as e:
    print("Erro NumPy: " + str(e))

try:
    import cv2
    print("OpenCV: " + cv2.__version__)
except ImportError as e:
    print("Erro OpenCV: " + str(e))

try:
    import flask
    print("Flask: " + flask.__version__)
except ImportError as e:
    print("Erro Flask: " + str(e))

try:
    import requests
    print("Requests: " + requests.__version__)
except ImportError as e:
    print("Erro Requests: " + str(e))

# Testa câmera
try:
    cap = cv2.VideoCapture(0)
    if cap.isOpened():
        print("Camera: OK")
        cap.release()
    else:
        print("Camera: ERRO")
except Exception as e:
    print("Erro Camera: " + str(e))
'@
        
        $testScript | & "py" "-$version" "-c" "exec(input())"
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Python $version: TODAS DEPENDENCIAS OK!" -ForegroundColor Green
        } else {
            Write-Host "Python $version: ALGUMAS DEPENDENCIAS COM PROBLEMA" -ForegroundColor Yellow
        }
        
    } catch {
        Write-Host "Python $version nao encontrado ou com erro: $_" -ForegroundColor Red
    }
}

Write-Host "`nRecomendacoes:" -ForegroundColor Cyan
Write-Host "1. Use Python 3.11 para melhor compatibilidade" -ForegroundColor Yellow
Write-Host "2. Se houver problemas, execute: .\install-python-deps-311.ps1" -ForegroundColor Yellow
Write-Host "3. Para Python 3.13, use versoes especificas" -ForegroundColor Yellow

Read-Host "`nPressione Enter para sair" 