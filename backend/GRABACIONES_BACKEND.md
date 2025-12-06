# Sistema de Grabaciones de Llamadas - Implementación Backend COMPLETADA

## 📋 Resumen
Sistema completo de grabación, almacenamiento y transcripción automática de llamadas telefónicas con IA.

## ✅ Archivos Creados

### Database
- `V011__create_grabacion_llamada_table.sql` - Migración Flyway con tabla completa

### Entities
- `GrabacionLlamadaEntity.java` - Entidad JPA con relaciones y enums

### DTOs
- `GrabacionDTO.java` - DTO para transferencia de datos
- `SubirGrabacionRequest.java` - Request para subida de archivos

### Repositories
- `GrabacionLlamadaRepository.java` - Repository con queries personalizadas

### Configuration
- `FirebaseConfig.java` - Configuración Firebase Admin SDK (lee desde .env)
- `.env.example` - Template de variables de entorno

### Services
- `FirebaseStorageService.java` - Gestión de archivos MP3 en Firebase Storage
- `GeminiTranscriptionService.java` - Transcripción con Gemini AI (lee desde .env)
- `GrabacionService.java` - Servicio principal orquestador

### Controller
- `TelemarketingController.java` - 6 nuevos endpoints REST

## 🔑 Variables de Entorno Requeridas

Agrega estas variables a tu archivo `.env`:

```bash
# Firebase Storage
FIREBASE_CREDENTIALS_PATH=path/to/firebase-service-account.json
FIREBASE_STORAGE_BUCKET=your-project.appspot.com

# Gemini AI
GEMINI_API_KEY=your-gemini-api-key
GEMINI_MODEL=gemini-2.0-flash-exp
```

## 📡 API Endpoints Creados

### 1. Subir Grabación
```http
POST /api/v1/campanias-telefonicas/{idCampania}/grabaciones
Content-Type: multipart/form-data

Parameters:
- archivo: File (audio/*)
- idLead: Long
- idLlamada: Integer (opcional)
- duracionSegundos: Integer
- resultado: String (opcional)
```

### 2. Listar Grabaciones
```http
GET /api/v1/agentes/me/grabaciones?page=0&size=20

Query Parameters (opcionales):
- idCampania: Integer
- resultado: String
- fechaDesde: DateTime (ISO)
- fechaHasta: DateTime (ISO)
- busqueda: String (nombre o teléfono)
- page: int (default: 0)
- size: int (default: 20)
```

### 3. Obtener Grabación
```http
GET /api/v1/grabaciones/{idGrabacion}
```

### 4. Obtener URL de Audio
```http
GET /api/v1/grabaciones/{idGrabacion}/audio

Response:
{
  "data": {
    "url": "https://storage.googleapis.com/..."
  }
}
```

### 5. Obtener Transcripción
```http
GET /api/v1/grabaciones/{idGrabacion}/transcripcion

Content-Type: text/markdown
```

### 6. Eliminar Grabación
```http
DELETE /api/v1/grabaciones/{idGrabacion}
```

## 🔄 Flujo de Procesamiento

1. **Subida**: Frontend sube archivo MP3
2. **Registro**: Se crea entrada en BD con estado `PENDIENTE`
3. **Firebase**: Audio se sube a Firebase Storage
4. **Async**: Proceso de transcripción inicia en segundo plano
   - Estado cambia a `PROCESANDO`
   - Gemini transcribe el audio
   - Transcripción se guarda en Supabase
   - Estado cambia a `COMPLETADO`
5. **Retry**: Si falla, estado cambia a `ERROR` con mensaje

## 📁 Estructura de Almacenamiento

### Firebase Storage
```
grabaciones/
├── {idCampania}/
│   └── {idAgente}/
│       └── YYYYMMDD_HHmmss_{idLead}.mp3
```

### Supabase Bucket: `grabaciones_llamada`
```
grabaciones_llamada/
├── {idCampania}/
│   └── {idAgente}/
│       └── YYYYMMDD_HHmmss_{idLead}.md
```

## 🤖 Prompt de Gemini

El sistema usa un prompt especializado que:
- Identifica 2 hablantes (Agente y Cliente)
- Genera timestamps en formato MM:SS
- Produce transcripción en Markdown
- Incluye resumen con puntos clave y sentimiento

## 🛡️ Seguridad

- ✅ Autenticación por agente
- ✅ Validación de permisos (solo acceso a propias grabaciones)
- ✅ Validación de archivos (tipo audio, max 50MB)
- ✅ URLs firmadas temporales (1 hora de validez)

## 📊 Estados de Procesamiento

- `PENDIENTE`: Archivo subido, esperando procesamiento
- `PROCESANDO`: Transcripción en curso
- `COMPLETADO`: Transcripción lista
- `ERROR`: Falló el procesamiento (ver mensajeError)

## 🔧 Configuración Adicional

### Habilitar Async en Spring Boot
Ya debería estar habilitado si tienes `@EnableAsync` en tu Application class.

### Bucket de Supabase
El bucket `grabaciones_llamada` debe existir en Supabase. Créalo si no existe.

## 🚀 Próximos Pasos

1. **Frontend**: Implementar componentes React para:
   - Grabación de audio durante llamadas
   - Página de gestión de grabaciones
   - Reproductor de audio
   - Visor de transcripciones

2. **Testing**: Probar flujo completo con audio real

3. **Optimización**: Considerar compresión de audio antes de subir

## ⚠️ Notas Importantes

- Las transcripciones con Gemini tienen costo por uso
- Firebase Storage también tiene costos (revisar pricing)
- Los archivos .mp3 se almacenan en Firebase (no en Supabase)
- Las transcripciones .md se almacenan en Supabase (no en Firebase)
