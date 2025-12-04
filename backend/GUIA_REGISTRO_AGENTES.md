# Guía de Uso: Utilidad de Registro de Agentes de Marketing

Esta utilidad permite registrar nuevos agentes de marketing en el sistema CRM a través de una consola interactiva, asegurando que las contraseñas se almacenen de forma segura (hasheadas) y que se asignen los roles y datos correctos.

## Requisitos Previos

1.  Tener **Java 17** o superior instalado.
2.  Tener acceso a la base de datos configurada en `application.yml`.
3.  Estar ubicado en la carpeta `backend` del proyecto.

## Cómo Ejecutar

### Opción 1: Script Automático (Recomendado)

Hemos incluido un script de Windows para facilitar la ejecución.

1.  Abra una terminal (CMD o PowerShell) en la carpeta `backend`.
2.  Ejecute el siguiente comando:

    ```cmd
    .\register-agent.bat
    ```

3.  Alternativamente, puede hacer **doble clic** en el archivo `register-agent.bat` desde el Explorador de Archivos.

**Nota:** Al iniciar, verá un mensaje indicando que la utilidad está cargando. Esto puede tardar unos segundos mientras se conecta a la base de datos. Hemos optimizado el script para reducir el ruido en la consola.

### Opción 2: Ejecución Manual con Maven

Si prefiere usar Maven directamente o está en un entorno diferente:

```bash
.\mvnw.cmd -q spring-boot:run -Dspring-boot.run.arguments="--register-agent" -Dspring-boot.run.jvmArguments="-Dspring.main.web-application-type=none -Dlogging.level.root=ERROR -Dlogging.level.org.springframework=ERROR -Dlogging.level.pe.unmsm.crm.marketing=INFO -Dspring.main.banner-mode=off"
```

## Pasos del Registro

Una vez iniciada la utilidad, siga las instrucciones en pantalla:

1.  **Confirmación**: Se le preguntará si desea registrar un nuevo agente. Escriba `S` y presione Enter.
2.  **Datos Personales**: Ingrese el Nombre Completo, Email y Teléfono del agente.
3.  **Credenciales**:
    *   **Username**: Debe ser único en el sistema.
    *   **Password**: La contraseña será encriptada automáticamente antes de guardarse.
4.  **Selección de Rol**: Se listarán los roles disponibles (ej. ADMIN, AGENTE). Ingrese el **ID** numérico del rol deseado.
5.  **ID Trabajador RRHH**: Ingrese el ID numérico correspondiente al trabajador en el sistema de Recursos Humanos. Este campo es obligatorio.

## Notas Importantes

*   La utilidad se ejecuta en modo "no-web", por lo que **no** iniciará el servidor Tomcat ni ocupará el puerto 8080. Esto permite ejecutarla incluso si la aplicación principal ya está corriendo.
*   Si ingresa un username que ya existe, la utilidad le pedirá que elija otro.
*   Al finalizar el registro, la utilidad se cerrará automáticamente.
