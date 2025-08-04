Write-Host "Iniciando Sistema Hibrido TouchVirtual (Java + Python)" -ForegroundColor Green
Write-Host ""

# Verifica se Python está instalado
try {
    $pythonVersion = python --version 2>&1
    Write-Host "Python encontrado: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Host "Python nao encontrado! Instale Python 3.8+ primeiro." -ForegroundColor Red
    Write-Host "Baixe em: https://www.python.org/downloads/" -ForegroundColor Yellow
    Read-Host "Pressione Enter para sair"
    exit 1
}

Write-Host ""

# Verifica se as dependências Python estão instaladas
Write-Host "Verificando dependencias Python..." -ForegroundColor Yellow
try {
    py -3.13 -c "import cv2, numpy, flask, requests" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Dependencias Python OK" -ForegroundColor Green
    } else {
        Write-Host "Dependencias Python nao encontradas!" -ForegroundColor Red
        Write-Host "Execute: .\install-python-deps.ps1" -ForegroundColor Yellow
        Read-Host "Pressione Enter para sair"
        exit 1
    }
} catch {
    Write-Host "Dependencias Python nao encontradas!" -ForegroundColor Red
    Write-Host "Execute: .\install-python-deps.ps1" -ForegroundColor Yellow
    Read-Host "Pressione Enter para sair"
    exit 1
}

Write-Host ""

# Inicia o serviço Python em background
Write-Host "Iniciando servico Python de deteccao..." -ForegroundColor Yellow
Start-Process -FilePath "py" -ArgumentList "-3.13 hand_detection_service_opencv.py" -WindowStyle Hidden

# Aguarda um pouco para o Python inicializar
Start-Sleep -Seconds 3

# Verifica se o Python está rodando
Write-Host "Verificando se o servico Python esta rodando..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:5000/api/hand-detection/health" -TimeoutSec 5 -ErrorAction SilentlyContinue
    if ($response.StatusCode -eq 200) {
        Write-Host "Servico Python OK" -ForegroundColor Green
    } else {
        Write-Host "Servico Python nao esta respondendo!" -ForegroundColor Red
        Write-Host "Verifique se a porta 5000 esta livre" -ForegroundColor Yellow
        Read-Host "Pressione Enter para sair"
        exit 1
    }
} catch {
    Write-Host "Servico Python nao esta respondendo!" -ForegroundColor Red
    Write-Host "Verifique se a porta 5000 esta livre" -ForegroundColor Yellow
    Read-Host "Pressione Enter para sair"
    exit 1
}

Write-Host ""

# Inicia o sistema Java
Write-Host "Iniciando sistema Java..." -ForegroundColor Yellow
Write-Host ""

# Executa o sistema Java
try {
    .\mvnw.cmd spring-boot:run
} catch {
    Write-Host "Erro ao iniciar sistema Java: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "Sistema parado" -ForegroundColor Yellow
Read-Host "Pressione Enter para sair" 