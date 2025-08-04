@echo off
echo 🚀 Iniciando TouchVirtual...
echo.

REM Desabilita modo headless para permitir Robot
set JAVA_OPTS=-Djava.awt.headless=false

REM Compila e executa a aplicação
call mvn clean compile
call mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.awt.headless=false"

echo.
echo ✅ TouchVirtual iniciado com sucesso!
echo 📱 Acesse: http://localhost:8082
echo 🎯 Sistema de touchscreen virtual ativo
pause 