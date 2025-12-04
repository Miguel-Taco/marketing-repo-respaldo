@echo off
echo ================================================
echo  REGISTRO DE AGENTES - MARKETING CRM
echo ================================================
echo.
echo Iniciando utilidad... (Esto puede tardar unos segundos mientras se conecta a la base de datos)
echo.

cd /d "%~dp0"
call mvnw.cmd -q spring-boot:run -Dspring-boot.run.profiles=console -Dspring-boot.run.jvmArguments="-Dspring.main.web-application-type=none -Dlogging.level.root=ERROR -Dlogging.level.org.springframework=ERROR -Dlogging.level.pe.unmsm.crm.marketing=INFO -Dlogging.level.org.hibernate.SQL=OFF -Dlogging.level.org.hibernate.type.descriptor.sql.BasicBinder=OFF -Dspring.main.banner-mode=off"

pause
