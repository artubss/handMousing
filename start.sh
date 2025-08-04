#!/bin/bash

echo "🚀 Iniciando TouchVirtual..."
echo

# Desabilita modo headless para permitir Robot
export JAVA_OPTS="-Djava.awt.headless=false"

# Compila e executa a aplicação
mvn clean compile
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.awt.headless=false"

echo
echo "✅ TouchVirtual iniciado com sucesso!"
echo "📱 Acesse: http://localhost:8082"
echo "🎯 Sistema de touchscreen virtual ativo" 