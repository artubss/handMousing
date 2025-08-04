@echo off
echo DIAGNOSTICO COMPLETO DA CAMERA...
echo.

echo === STEP 1: Verificando drivers de camera ===
powershell -Command "Get-PnpDevice | Where-Object {$_.Class -eq 'Camera' -or $_.FriendlyName -like '*camera*'} | Format-Table FriendlyName, Status, ProblemCode"

echo.
echo === STEP 2: Verificando servicos de camera ===
sc query FrameServer
sc query "Windows Camera Frame Server"

echo.
echo === STEP 3: Verificando permissoes de camera ===
powershell -Command "Get-AppxPackage *camera* | Format-Table Name, PackageFullName"

echo.
echo === STEP 4: Testando acesso direto a camera ===
powershell -Command "Add-Type -AssemblyName System.Drawing; try { $cam = New-Object System.Drawing.Imaging.ImageCodecInfo; Write-Host 'Camera acessivel via .NET' -ForegroundColor Green } catch { Write-Host 'Camera NAO acessivel via .NET' -ForegroundColor Red }"

echo.
echo === STEP 5: Verificando registro do Windows ===
reg query "HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows Media Foundation\Platform" /v EnableFrameServerMode 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Frame Server Mode encontrado
) else (
    echo Frame Server Mode NAO encontrado - pode ser o problema!
)

echo.
echo SOLUCOES SUGERIDAS:
echo 1. Reiniciar servicos de camera
echo 2. Atualizar drivers de camera
echo 3. Verificar configuracoes de privacidade
echo 4. Testar com aplicativo Camera nativo do Windows
pause