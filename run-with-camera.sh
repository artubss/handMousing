#!/bin/bash

echo "🎥 TouchVirtual - Iniciando com Câmera Real"
echo

# Libera a câmera de outros processos
echo "🔧 Liberando câmera de outros processos..."
lsof /dev/video0 2>/dev/null | grep -v COMMAND | awk '{print $2}' | xargs -r kill -9 2>/dev/null

echo
echo "🚀 Iniciando TouchVirtual com configurações otimizadas..."
echo "💡 Robot: Habilitado (modo GUI)"
echo "🎥 Câmera: Real (não simulada)"
echo

# Configura display para GUI
export DISPLAY=${DISPLAY:-:0}

# Executa com todas as configurações necessárias
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.awt.headless=false -Dfile.encoding=UTF-8 -Xmx2048m"

echo
echo "✅ TouchVirtual finalizado"