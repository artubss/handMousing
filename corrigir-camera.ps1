# Corrige acesso programático à câmera - EXECUTE COMO ADMINISTRADOR
Write-Host "CORRIGINDO ACESSO PROGRAMATICO A CAMERA..." -ForegroundColor Yellow
Write-Host ""

# Verifica se está executando como administrador
if (-NOT ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Host "ERRO: Execute como Administrador!" -ForegroundColor Red
    Write-Host "Clique direito no PowerShell > Executar como Administrador" -ForegroundColor Yellow
    pause
    exit
}

Write-Host "Habilitando Frame Server Mode..." -ForegroundColor Green
try {
    Set-ItemProperty -Path "HKLM:\SOFTWARE\Microsoft\Windows Media Foundation\Platform" -Name "EnableFrameServerMode" -Value 1 -Type DWord -Force
    Set-ItemProperty -Path "HKLM:\SOFTWARE\WOW6432Node\Microsoft\Windows Media Foundation\Platform" -Name "EnableFrameServerMode" -Value 1 -Type DWord -Force
    Write-Host "✅ Frame Server Mode habilitado" -ForegroundColor Green
} catch {
    Write-Host "❌ Erro ao configurar Frame Server Mode" -ForegroundColor Red
}

Write-Host "Configurando permissoes de camera..." -ForegroundColor Green
try {
    # Permissões para usuário atual
    New-Item -Path "HKCU:\SOFTWARE\Microsoft\Windows\CurrentVersion\CapabilityAccessManager\ConsentStore\webcam" -Force | Out-Null
    Set-ItemProperty -Path "HKCU:\SOFTWARE\Microsoft\Windows\CurrentVersion\CapabilityAccessManager\ConsentStore\webcam" -Name "Value" -Value "Allow" -Force
    
    New-Item -Path "HKCU:\SOFTWARE\Microsoft\Windows\CurrentVersion\CapabilityAccessManager\ConsentStore\webcam\NonPackaged" -Force | Out-Null
    Set-ItemProperty -Path "HKCU:\SOFTWARE\Microsoft\Windows\CurrentVersion\CapabilityAccessManager\ConsentStore\webcam\NonPackaged" -Name "Value" -Value "Allow" -Force
    
    # Permissões globais
    New-Item -Path "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\CapabilityAccessManager\ConsentStore\webcam" -Force | Out-Null
    Set-ItemProperty -Path "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\CapabilityAccessManager\ConsentStore\webcam" -Name "Value" -Value "Allow" -Force
    
    New-Item -Path "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\CapabilityAccessManager\ConsentStore\webcam\NonPackaged" -Force | Out-Null
    Set-ItemProperty -Path "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\CapabilityAccessManager\ConsentStore\webcam\NonPackaged" -Name "Value" -Value "Allow" -Force
    
    Write-Host "✅ Permissoes de camera configuradas" -ForegroundColor Green
} catch {
    Write-Host "❌ Erro ao configurar permissoes" -ForegroundColor Red
}

Write-Host "Configurando politicas de privacidade..." -ForegroundColor Green
try {
    New-Item -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\AppPrivacy" -Force | Out-Null
    Set-ItemProperty -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\AppPrivacy" -Name "LetAppsAccessCamera" -Value 1 -Type DWord -Force
    Write-Host "✅ Politicas configuradas" -ForegroundColor Green
} catch {
    Write-Host "❌ Erro ao configurar politicas" -ForegroundColor Red
}

Write-Host "Reiniciando servicos..." -ForegroundColor Green
try {
    Restart-Service FrameServer -Force
    Write-Host "✅ Servicos reiniciados" -ForegroundColor Green
} catch {
    Write-Host "⚠️ Erro ao reiniciar servicos" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "CONFIGURACAO CONCLUIDA!" -ForegroundColor Green
Write-Host "REINICIE O COMPUTADOR e execute: .\run-with-camera.bat" -ForegroundColor Yellow
Write-Host ""
Write-Host "Pressione qualquer tecla para continuar..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")