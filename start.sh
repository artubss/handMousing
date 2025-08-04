#!/bin/bash

echo "========================================"
echo "   Touch Virtual - Inicializando..."
echo "========================================"

# Verifica se o Java está instalado
if ! command -v java &> /dev/null; then
    echo "ERRO: Java não encontrado!"
    echo "Por favor, instale o Java 17 ou superior"
    exit 1
fi

# Verifica se o Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo "ERRO: Maven não encontrado!"
    echo "Por favor, instale o Maven 3.6 ou superior"
    exit 1
fi

echo ""
echo "Compilando o projeto..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "ERRO: Falha na compilação!"
    exit 1
fi

echo ""
echo "Iniciando a aplicação..."
echo "Acesse: http://localhost:8080"
echo ""
echo "Pressione Ctrl+C para parar"
echo ""

mvn spring-boot:run 