@echo off
setlocal EnableDelayedExpansion
echo ================================================
echo  REGISTRO DE AGENTES - MARKETING CRM
echo ================================================
echo.

cd /d "%~dp0"

set JAR_FILE=target\marketing-backend-0.0.1-SNAPSHOT.jar

REM Check if JAR exists
if exist "%JAR_FILE%" (
    echo Detectado JAR existente en: %JAR_FILE%
    echo.
    set /p USE_EXISTING="¿Desea usarlo directamente? (S/N): "
    if /i "!USE_EXISTING!"=="S" (
        goto :RUN_JAR
    )
    if /i "!USE_EXISTING!"=="N" (
        goto :BUILD
    )
    echo Opcion no valida, usando JAR existente...
    goto :RUN_JAR
) else (
    echo No se encontro JAR compilado. Iniciando compilacion...
    goto :BUILD
)

:BUILD
echo.
echo ================================================
echo  COMPILANDO APLICACION
echo ================================================
echo.
call mvnw.cmd clean package -DskipTests
if errorlevel 1 (
    echo.
    echo ERROR: La compilacion fallo.
    pause
    exit /b 1
)
echo.
echo Compilacion exitosa!
echo.

:RUN_JAR
echo ================================================
echo  INICIANDO UTILIDAD
echo ================================================
echo.
echo Conectando a la base de datos...
echo.

java -jar "%JAR_FILE%" ^
    --spring.profiles.active=console ^
    --spring.main.web-application-type=none ^
    --logging.level.root=ERROR ^
    --logging.level.org.springframework=ERROR ^
    --logging.level.pe.unmsm.crm.marketing=INFO ^
    --logging.level.org.hibernate.SQL=OFF ^
    --logging.level.org.hibernate.type.descriptor.sql.BasicBinder=OFF ^
    --spring.main.banner-mode=off

pause
