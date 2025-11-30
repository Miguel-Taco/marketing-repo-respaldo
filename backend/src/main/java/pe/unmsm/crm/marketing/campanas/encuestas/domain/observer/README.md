# Estructura del Patrón Observer en Encuestas

## Ubicación
`backend/src/main/java/pe/unmsm/crm/marketing/campanas/encuestas/domain/observer/`

## Archivos del Patrón Observer

### 1. AlertaUrgenteDetectadaEvent.java
**Rol**: Evento (Subject notification)
- Evento inmutable que se publica cuando se detecta una alerta urgente
- Contiene: `idLead`, `idEncuesta`, `timestamp`

### 2. AlertaUrgenteEventListener.java
**Rol**: Observer concreto (Listener principal)
- Escucha eventos de alertas urgentes
- Notifica al módulo de telemarketing vía HTTP
- Usa `@TransactionalEventListener` para ejecutarse después del commit

### 3. AlertaUrgenteLoggingListener.java
**Rol**: Observer concreto (Listener de ejemplo)
- Demuestra la extensibilidad del patrón
- Registra alertas urgentes en logs
- Usa `@EventListener` para procesamiento síncrono

## Publisher (Publicador)
`RespuestaEncuestaService.java` (ubicado en `application/service/`)
- Publica eventos usando `ApplicationEventPublisher`
- No conoce a los listeners (desacoplado)

## Diagrama de Estructura

```
domain/observer/
├── AlertaUrgenteDetectadaEvent.java       (Evento)
├── AlertaUrgenteEventListener.java        (Observer 1)
└── AlertaUrgenteLoggingListener.java      (Observer 2)

application/service/
└── RespuestaEncuestaService.java          (Publisher)
```

## Ventajas de esta Organización

✅ **Claridad**: Todos los componentes del patrón Observer en un solo lugar
✅ **Mantenibilidad**: Fácil encontrar y modificar listeners
✅ **Extensibilidad**: Agregar nuevos observers en la misma carpeta
✅ **Documentación**: La estructura del código documenta el patrón
