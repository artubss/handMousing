# 🎥 Solução para Problemas de Câmera e Robot

## 🚨 Problemas Identificados:

1. **Robot em modo headless** - Precisa executar com GUI
2. **Câmera ocupada** - Erro `-1072875772` indica que a câmera está sendo usada por outro processo
3. **Código VideoCapture antigo** - Há referências antigas que precisam ser removidas

## ✅ SOLUÇÕES IMEDIATAS:

### 1. Para o Robot Funcionar:

Execute com este comando exato:

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.awt.headless=false -Dfile.encoding=UTF-8"
```

### 2. Para a Câmera Funcionar:

#### Feche todos os programas que podem estar usando a câmera:

- Skype
- Teams
- Discord
- Chrome (com páginas que usam câmera)
- Zoom
- OBS Studio
- Qualquer outro aplicativo de vídeo

#### Execute este comando no PowerShell como Administrador:

```powershell
# Para Windows - libera câmera
Get-Process | Where-Object {$_.ProcessName -like "*camera*" -or $_.ProcessName -like "*webcam*"} | Stop-Process -Force
```

#### Verifique se a câmera está disponível:

```bash
# Teste rápido
ffmpeg -list_devices true -f dshow -i dummy
```

### 3. Configuração Correta:

Crie um arquivo `run-with-camera.bat`:

```batch
@echo off
echo 🎥 Liberando câmera...
taskkill /f /im "Camera.exe" 2>nul
taskkill /f /im "WindowsCamera.exe" 2>nul

echo 🚀 Iniciando TouchVirtual com câmera real...
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.awt.headless=false -Dfile.encoding=UTF-8 -Djava.library.path=target/classes"

pause
```

### 4. Para Linux/Mac:

```bash
#!/bin/bash
echo "🎥 Verificando câmera..."
lsof /dev/video0 | grep -v COMMAND | awk '{print $2}' | xargs -r kill -9

echo "🚀 Iniciando TouchVirtual..."
export DISPLAY=:0
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.awt.headless=false"
```

## 🔧 Verificações:

### 1. Teste se a câmera está livre:

- Abra o aplicativo Câmera do Windows
- Se funcionar, feche e tente novamente o TouchVirtual

### 2. Verificar logs:

Procure por estas mensagens de SUCESSO:

```
🤖 Robot inicializado para simulação de eventos
✅ Câmera encontrada no índice 0: 640x480
```

### 3. Se ainda não funcionar:

```bash
# Reinicie o serviço de câmera do Windows
net stop "Windows Camera Frame Server"
net start "Windows Camera Frame Server"
```

## 🎯 Resultado Esperado:

Quando funcionar corretamente, você deve ver:

```
🤖 Robot inicializado para simulação de eventos
✅ Câmera encontrada no índice 0: 640x480
🔄 Processamento iniciado em background
✅ Detectados X landmarks de mão real
```

## 📞 Se ainda não funcionar:

1. Reinicie o computador
2. Execute como Administrador
3. Desabilite antivírus temporariamente
4. Verifique se não há outras aplicações Java rodando

---

**Execute o comando exato acima e a câmera + Robot devem funcionar!** 🎉
