@echo off
echo ========================================
echo    Touch Virtual - Inicializando...
echo ========================================

REM Verifica se o Java está instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERRO: Java nao encontrado!
    echo Por favor, instale o Java 17 ou superior
    pause
    exit /b 1
)

REM Verifica se o Maven está instalado
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERRO: Maven nao encontrado!
    echo Por favor, instale o Maven 3.6 ou superior
    pause
    exit /b 1
)

echo.
echo Compilando o projeto...
call mvn clean compile

if %errorlevel% neq 0 (
    echo ERRO: Falha na compilacao!
    pause
    exit /b 1
)

echo.
echo Iniciando a aplicacao...
echo Acesse: http://localhost:8080
echo.
echo Pressione Ctrl+C para parar
echo.

call mvn spring-boot:run

pause 