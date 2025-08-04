Write-Host "Instalando dependencias Python passo a passo..." -ForegroundColor Green

# Verifica se Python está instalado
try {
    $pythonVersion = py -3.13 --version 2>&1
    Write-Host "Python encontrado: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Host "Python nao encontrado! Instale Python 3.8+ primeiro." -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

Write-Host "Instalando dependencias uma por uma..." -ForegroundColor Yellow

# Instala numpy primeiro
Write-Host "Instalando numpy..." -ForegroundColor Yellow
py -3.13 -m pip install numpy
if ($LASTEXITCODE -ne 0) {
    Write-Host "Erro ao instalar numpy!" -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

# Instala opencv-python
Write-Host "Instalando opencv-python..." -ForegroundColor Yellow
py -3.13 -m pip install opencv-python
if ($LASTEXITCODE -ne 0) {
    Write-Host "Erro ao instalar opencv-python!" -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

# Instala flask
Write-Host "Instalando flask..." -ForegroundColor Yellow
py -3.13 -m pip install flask
if ($LASTEXITCODE -ne 0) {
    Write-Host "Erro ao instalar flask!" -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

# Instala requests
Write-Host "Instalando requests..." -ForegroundColor Yellow
py -3.13 -m pip install requests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Erro ao instalar requests!" -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}



Write-Host "Instalacao concluida!" -ForegroundColor Green
Read-Host "Pressione Enter para sair" 