Write-Host "Compilando sistema TouchVirtual..." -ForegroundColor Green

# Verifica se Maven está disponível
try {
    $mvnVersion = .\mvnw.cmd --version 2>&1
    Write-Host "Maven encontrado" -ForegroundColor Green
} catch {
    Write-Host "Maven nao encontrado!" -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

Write-Host "Compilando projeto..." -ForegroundColor Yellow

# Compila o projeto
try {
    .\mvnw.cmd compile -q
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Sistema compilado com sucesso!" -ForegroundColor Green
        Write-Host "Agora voce pode executar: .\start-hybrid-system.ps1" -ForegroundColor Cyan
    } else {
        Write-Host "Erro na compilacao!" -ForegroundColor Red
        Read-Host "Pressione Enter para sair"
        exit 1
    }
} catch {
    Write-Host "Erro na compilacao: $_" -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

Read-Host "Pressione Enter para sair" 