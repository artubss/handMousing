Write-Host "Testando dependencias Python..." -ForegroundColor Green

# Testa Python 3.11 primeiro (recomendado)
Write-Host "`nTestando Python 3.11..." -ForegroundColor Cyan
try {
    py -3.11 test-python-simple.py
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Python 3.11: OK!" -ForegroundColor Green
    } else {
        Write-Host "Python 3.11: PROBLEMA" -ForegroundColor Red
    }
} catch {
    Write-Host "Python 3.11 nao encontrado" -ForegroundColor Red
}

# Testa Python 3.13
Write-Host "`nTestando Python 3.13..." -ForegroundColor Cyan
try {
    py -3.13 test-python-simple.py
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Python 3.13: OK!" -ForegroundColor Green
    } else {
        Write-Host "Python 3.13: PROBLEMA" -ForegroundColor Red
    }
} catch {
    Write-Host "Python 3.13 nao encontrado" -ForegroundColor Red
}

Write-Host "`nRecomendacoes:" -ForegroundColor Cyan
Write-Host "1. Use Python 3.11 para melhor compatibilidade" -ForegroundColor Yellow
Write-Host "2. Se houver problemas, execute: .\install-python-deps-311.ps1" -ForegroundColor Yellow
Write-Host "3. Para Python 3.13, use versoes especificas" -ForegroundColor Yellow

Read-Host "`nPressione Enter para sair" 