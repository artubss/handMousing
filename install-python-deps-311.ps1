Write-Host "Instalando dependencias Python 3.11 para deteccao de maos..." -ForegroundColor Green

# Verifica se Python 3.11 está instalado
try {
    $pythonVersion = py -3.11 --version 2>&1
    Write-Host "Python 3.11 encontrado: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Host "Python 3.11 nao encontrado!" -ForegroundColor Red
    Write-Host "Recomendado: Instale Python 3.11 para melhor compatibilidade" -ForegroundColor Yellow
    Write-Host "Baixe em: https://www.python.org/downloads/release/python-3118/" -ForegroundColor Cyan
    Write-Host "Ou use Python 3.13 com versoes especificas" -ForegroundColor Yellow
    Read-Host "Pressione Enter para continuar com Python 3.13"
    
    # Tenta com Python 3.13
    try {
        $pythonVersion = py -3.13 --version 2>&1
        Write-Host "Usando Python 3.13: $pythonVersion" -ForegroundColor Green
    } catch {
        Write-Host "Nenhum Python encontrado!" -ForegroundColor Red
        Read-Host "Pressione Enter para sair"
        exit 1
    }
}

Write-Host "Instalando versoes estaveis..." -ForegroundColor Yellow

# Instala as dependências com versões estáveis
try {
    # Usa Python 3.11 se disponível, senão 3.13
    $pythonCmd = "py -3.11"
    try {
        py -3.11 --version 2>&1 | Out-Null
    } catch {
        $pythonCmd = "py -3.13"
    }
    
    Write-Host "Usando comando: $pythonCmd" -ForegroundColor Cyan
    
    # Desinstala versões conflitantes
    Write-Host "Limpando instalacoes anteriores..." -ForegroundColor Yellow
    & $pythonCmd -m pip uninstall numpy opencv-python -y 2>$null
    & $pythonCmd -m pip cache purge 2>$null
    
    # Instala versões compatíveis
    Write-Host "Instalando NumPy 1.24.3..." -ForegroundColor Yellow
    & $pythonCmd -m pip install "numpy==1.24.3"
    
    Write-Host "Instalando OpenCV 4.8.1.78..." -ForegroundColor Yellow
    & $pythonCmd -m pip install "opencv-python==4.8.1.78"
    
    Write-Host "Instalando Flask e Requests..." -ForegroundColor Yellow
    & $pythonCmd -m pip install "flask==3.0.0" "requests==2.31.0"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Dependencias instaladas com sucesso!" -ForegroundColor Green
        Write-Host "Versoes instaladas:" -ForegroundColor Cyan
        & $pythonCmd -m pip list | findstr -i "numpy opencv flask requests"
        Write-Host "Agora voce pode executar: $pythonCmd hand_detection_service_opencv.py" -ForegroundColor Cyan
    } else {
        Write-Host "Erro ao instalar dependencias!" -ForegroundColor Red
        Read-Host "Pressione Enter para sair"
        exit 1
    }
} catch {
    Write-Host "Erro ao instalar dependencias: $_" -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

Read-Host "Pressione Enter para sair" 