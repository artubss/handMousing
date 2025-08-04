# PowerShell script para liberar camera
Write-Host "LIBERANDO CAMERA FORCADAMENTE..." -ForegroundColor Yellow
Write-Host ""

Write-Host "Parando aplicativo Camera do Windows..." -ForegroundColor Green
Get-Process -Name "Camera", "WindowsCamera" -ErrorAction SilentlyContinue | Stop-Process -Force

Write-Host "Parando programas de video..." -ForegroundColor Green  
Get-Process -Name "SkypeApp", "Teams", "Discord", "obs64", "Zoom" -ErrorAction SilentlyContinue | Stop-Process -Force

Write-Host "Parando navegadores e WebView2..." -ForegroundColor Green
Get-Process -Name "chrome", "firefox", "msedge", "msedgewebview2" -ErrorAction SilentlyContinue | Stop-Process -Force

Write-Host "Reiniciando servicos de camera..." -ForegroundColor Green
try {
    Stop-Service "FrameServer" -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
    Start-Service "FrameServer" -ErrorAction SilentlyContinue
} catch {
    Write-Host "Servico FrameServer nao encontrado, continuando..." -ForegroundColor Yellow
}

Write-Host "Verificando cameras disponiveis..." -ForegroundColor Green
Get-PnpDevice | Where-Object {$_.FriendlyName -like "*camera*" -or $_.FriendlyName -like "*webcam*" -or $_.Class -eq "Camera"} | Format-Table FriendlyName, Status

Write-Host ""
Write-Host "CAMERA LIBERADA! Execute agora: .\run-with-camera.bat" -ForegroundColor Green
Write-Host "Pressione qualquer tecla para continuar..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")