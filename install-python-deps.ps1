Write-Host "Instalando dependencias Python para deteccao de maos..." -ForegroundColor Green

# Verifica se Python está instalado
try {
    $pythonVersion = py -3.13 --version 2>&1
    Write-Host "Python encontrado: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Host "Python nao encontrado! Instale Python 3.8+ primeiro." -ForegroundColor Red
    Write-Host "Baixe em: https://www.python.org/downloads/" -ForegroundColor Yellow
    Read-Host "Pressione Enter para sair"
    exit 1
}

Write-Host "Resolvendo conflito NumPy/OpenCV..." -ForegroundColor Yellow

# Desinstala versões conflitantes
try {
    Write-Host "Desinstalando versoes conflitantes..." -ForegroundColor Yellow
    py -3.13 -m pip uninstall numpy opencv-python -y
    py -3.13 -m pip cache purge
} catch {
    Write-Host "Aviso: Erro ao limpar cache: $_" -ForegroundColor Yellow
}

Write-Host "Instalando versoes compatíveis..." -ForegroundColor Yellow

# Instala as dependências com versões específicas
try {
    # Instala NumPy primeiro (versão compatível)
    py -3.13 -m pip install "numpy==1.24.3"
    
    # Instala OpenCV (versão compatível com NumPy 1.x)
    py -3.13 -m pip install "opencv-python==4.8.1.78"
    
    # Instala outras dependências
    py -3.13 -m pip install "flask==3.0.0" "requests==2.31.0"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Dependencias instaladas com sucesso!" -ForegroundColor Green
        Write-Host "Versoes instaladas:" -ForegroundColor Cyan
        py -3.13 -m pip list | findstr -i "numpy opencv flask requests"
        Write-Host "Agora voce pode executar: py -3.13 hand_detection_service_opencv.py" -ForegroundColor Cyan
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