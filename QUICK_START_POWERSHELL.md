# 🚀 Guia Rápido - Sistema Híbrido TouchVirtual (PowerShell)

## 📋 Pré-requisitos

- Python 3.11+ ou 3.13+ instalado
- Java 21+ instalado
- PowerShell 5.0+

## 🛠️ Instalação Rápida

### 1. Instalar Dependências Python (Versões Compatíveis)

```powershell
# Opção 1: Instalação com Python 3.11 (recomendado)
.\install-python-deps-311.ps1

# Opção 2: Instalação com Python 3.13 (versões específicas)
.\install-python-deps.ps1

# Opção 3: Instalação manual
py -3.11 -m pip install "numpy==1.24.3" "opencv-python==4.8.1.78" "flask==3.0.0" "requests==2.31.0"
```

### 2. Compilar Sistema Java

```powershell
.\compile-system.ps1
```

### 3. Iniciar Sistema Híbrido

```powershell
.\start-hybrid-system.ps1
```

## 🔧 Comandos Manuais

### Instalar Dependências Python (Versões Compatíveis)

```powershell
# Para Python 3.11 (recomendado)
py -3.11 -m pip install "numpy==1.24.3" "opencv-python==4.8.1.78" "flask==3.0.0" "requests==2.31.0"

# Para Python 3.13 (se necessário)
py -3.13 -m pip install "numpy==1.24.3" "opencv-python==4.8.1.78" "flask==3.0.0" "requests==2.31.0"
```

### Compilar Java

```powershell
.\mvnw.cmd compile
```

### Executar Sistema

```powershell
# Terminal 1: Python (OpenCV) - Python 3.11
py -3.11 hand_detection_service_opencv.py

# Terminal 2: Java
.\mvnw.cmd spring-boot:run
```

## 📊 Verificar Status

### Python

```powershell
Invoke-WebRequest -Uri "http://localhost:5000/api/hand-detection/health"
```

### Java

```powershell
Invoke-WebRequest -Uri "http://localhost:8082/api/gestures/status"
```

## 🔍 Troubleshooting

### Erro: Conflito NumPy/OpenCV

```powershell
# Solução 1: Usar Python 3.11 (recomendado)
py -3.11 -m pip uninstall numpy opencv-python -y
py -3.11 -m pip install "numpy==1.24.3" "opencv-python==4.8.1.78"

# Solução 2: Usar versões específicas no Python 3.13
py -3.13 -m pip uninstall numpy opencv-python -y
py -3.13 -m pip install "numpy==1.24.3" "opencv-python==4.8.1.78"
```

### Erro: Python não encontrado

```powershell
# Verificar Python
python --version
py -3.11 --version
py -3.13 --version

# Se não encontrado, baixe Python 3.11:
# https://www.python.org/downloads/release/python-3118/
```

### Erro: Dependências Python

```powershell
# Reinstalar dependências com versões específicas
py -3.11 -m pip install --upgrade "numpy==1.24.3" "opencv-python==4.8.1.78" "flask==3.0.0" "requests==2.31.0"
```

### Erro: Porta ocupada

```powershell
# Verificar portas
netstat -an | findstr :5000
netstat -an | findstr :8082
```

### Erro: Câmera não funciona

```powershell
# Testar câmera
py -3.11 -c "import cv2; cap = cv2.VideoCapture(0); print('OK' if cap.isOpened() else 'ERRO')"
```

## 🎯 Acesso ao Sistema

Após iniciar, acesse:

- **Interface Web**: http://localhost:8082
- **API Python**: http://localhost:5000
- **API Java**: http://localhost:8082/api

## 📁 Arquivos Importantes

- `hand_detection_service_opencv.py` - Serviço Python (OpenCV)
- `requirements.txt` - Dependências Python (versões específicas)
- `install-python-deps.ps1` - Instalador Python 3.13
- `install-python-deps-311.ps1` - Instalador Python 3.11 (recomendado)
- `compile-system.ps1` - Compilador Java
- `start-hybrid-system.ps1` - Iniciador completo

## 🚀 Próximos Passos

1. Execute `.\install-python-deps-311.ps1` (recomendado)
2. Execute `.\compile-system.ps1`
3. Execute `.\start-hybrid-system.ps1`
4. Acesse http://localhost:8082
5. Teste a detecção de mãos!

## 📝 Notas Importantes

- **Python 3.11** é mais estável para OpenCV
- **NumPy 1.24.3** é compatível com OpenCV 4.8.1.78
- Evite NumPy 2.x com OpenCV (causa conflitos)
- Use versões específicas para evitar incompatibilidades

---

**🎉 Sistema híbrido funcionando!** Python para detecção precisa + Java para sistema robusto.
