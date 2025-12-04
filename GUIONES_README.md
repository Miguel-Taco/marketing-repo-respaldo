# Gestión de Guiones con Supabase Storage

## 📋 Resumen de la Implementación

Se ha implementado un sistema completo de gestión de guiones en formato Markdown para campañas telefónicas, utilizando Supabase Storage como backend de almacenamiento.

## 🎯 Características Implementadas

### Backend (Java/Spring Boot)

✅ **Configuración de Supabase Storage**
- `SupabaseStorageConfig.java`: Lee credenciales desde variables de entorno
- `SupabaseStorageService.java`: Servicio genérico para operaciones con Supabase Storage

✅ **Gestión de Archivos**
- `GuionArchivo.java`: Entidad para metadatos de archivos
- `GuionArchivoRepository.java`: Repository con queries especializadas
- `GuionArchivoService.java`: Lógica de negocio para upload/download/delete
- `GuionArchivoDTO.java`: DTO para respuestas API

✅ **Endpoints REST**
- `POST /campanias-telefonicas/{id}/guiones/general` - Subir guión general
- `GET /campanias-telefonicas/{id}/guiones/general` - Listar guiones generales
- `POST /campanias-telefonicas/{id}/guiones/agente/{idAgente}` - Subir guión de agente
- `GET /campanias-telefonicas/{id}/guiones/agente/{idAgente}` - Listar guiones de agente
- `DELETE /guiones/{idGuion}` - Eliminar guión
- `GET /guiones/{idGuion}/download` - Descargar archivo
- `GET /guiones/{idGuion}/contenido` - Obtener contenido markdown

✅ **Validaciones**
- Solo acepta archivos con extensión `.md`
- Validación de tamaño de archivo
- Manejo de errores robusto

### Frontend (React/TypeScript)

✅ **Componentes**
- `MarkdownViewer.tsx`: Visualizador de markdown con `react-markdown` y `@tailwindcss/typography`
- `ScriptManagementPage.tsx`: Página completa de gestión con layout de 2 columnas

✅ **Funcionalidades UI**
- Selector de campaña
- Drag & drop para subir archivos `.md`
- Lista de guiones con información (nombre, tamaño, fecha)
- Visualizador de markdown con estilos modernos
- Descarga y eliminación de guiones
- Validación de formato en el cliente

✅ **Dependencias Instaladas**
- `react-markdown`: Para renderizar markdown
- `@tailwindcss/typography`: Para estilos de tipografía

## 📁 Estructura de Directorios en Supabase

```
guiones/
├── campana/{idCampania}/
│   ├── general/              ← Guiones predeterminados de la campaña
│   │   ├── guion-ventas.md
│   │   └── guion-retencion.md
│   └── {idAgente}/           ← Guiones específicos del agente
│       └── mi-guion.md
```

## 🚀 Pasos para Usar

### 1. Configurar Supabase (IMPORTANTE)

Antes de usar la funcionalidad, debes crear el bucket en Supabase:

1. Ve a tu proyecto en [Supabase Dashboard](https://app.supabase.com)
2. Navega a **Storage** en el menú lateral
3. Haz clic en **Create a new bucket**
4. Nombre del bucket: `guiones`
5. Configura como **público** o **privado** según tus necesidades de seguridad
6. Haz clic en **Create bucket**

### 2. Ejecutar Migración de Base de Datos

La tabla `guion_archivo` se creará automáticamente al iniciar el backend si usas Flyway/Liquibase, o ejecuta manualmente:

```sql
-- Ya está en: backend/src/main/resources/db/migration/V006__create_guion_archivo_table.sql
```

### 3. Iniciar el Backend

El backend ya está configurado para leer las variables de entorno de `.env`:

```bash
cd backend
.\mvnw.cmd spring-boot:run
```

### 4. Iniciar el Frontend

```bash
cd frontend
npm run dev
```

### 5. Acceder a la Página de Guiones

Navega a: `http://localhost:5173/marketing/campanas/telefonicas/guiones`

## 📝 Cómo Usar la Interfaz

### Subir un Guión

1. **Selecciona una campaña** del dropdown en la parte superior
2. **Arrastra un archivo `.md`** al área de drag & drop, o
3. **Haz clic en "Subir Guion"** y selecciona un archivo
4. El guión aparecerá automáticamente en la lista

### Visualizar un Guión

1. Haz clic en cualquier guión de la lista
2. El contenido se mostrará formateado en el panel derecho
3. Verás encabezados, listas, código, etc. con estilos modernos

### Descargar un Guión

1. Selecciona el guión que deseas descargar
2. Haz clic en el botón **"Descargar"**
3. El archivo `.md` se descargará a tu computadora

### Eliminar un Guión

1. Selecciona el guión que deseas eliminar
2. Haz clic en el botón **"Eliminar"**
3. Confirma la acción en el diálogo
4. El guión se eliminará tanto de la base de datos como de Supabase Storage

## 🧪 Archivos de Prueba

Se han creado dos archivos de ejemplo en el directorio raíz del proyecto:

- `guion-ejemplo-ventas.md` - Guión de ventas con estructura completa
- `guion-ejemplo-retencion.md` - Guión de retención de clientes

Puedes usar estos archivos para probar la funcionalidad de subida y visualización.

## 🔮 Funcionalidad Futura

### Guiones por Agente

Aunque actualmente la UI solo muestra guiones generales, el backend ya está preparado para manejar guiones específicos de agentes:

- Endpoints implementados: `/campanias-telefonicas/{id}/guiones/agente/{idAgente}`
- Estructura de directorios: `campana/{idCampania}/{idAgente}/`
- Solo falta implementar autenticación de agentes en el frontend

## ⚠️ Notas Importantes

1. **Solo archivos Markdown**: El sistema rechaza cualquier archivo que no sea `.md`
2. **Bucket requerido**: Debes crear el bucket `guiones` en Supabase antes de usar
3. **Variables de entorno**: Asegúrate de que `SUPABASE_URL` y `SUPABASE_SERVICE_KEY` estén configuradas
4. **Campaña requerida**: Debes seleccionar una campaña antes de subir guiones

## 🐛 Troubleshooting

### Error: "Las variables de entorno SUPABASE_URL y SUPABASE_SERVICE_KEY son requeridas"

**Solución**: Verifica que el archivo `backend/.env` contenga:
```
SUPABASE_URL=https://rujqfdpeyoekhzesiorf.supabase.co
SUPABASE_SERVICE_KEY=tu_service_key_aqui
```

### Error: "Error al subir archivo a Supabase"

**Solución**: 
1. Verifica que el bucket `guiones` existe en Supabase
2. Verifica que las credenciales sean correctas
3. Revisa los logs del backend para más detalles

### Los guiones no se visualizan correctamente

**Solución**:
1. Verifica que el archivo sea markdown válido
2. Revisa la consola del navegador para errores
3. Asegúrate de que `@tailwindcss/typography` esté instalado

## 📚 Tecnologías Utilizadas

- **Backend**: Spring Boot, Supabase Storage API, JPA/Hibernate
- **Frontend**: React, TypeScript, react-markdown, Tailwind CSS, @tailwindcss/typography
- **Storage**: Supabase Storage
- **Base de Datos**: MySQL (metadatos)

## ✅ Checklist de Verificación

- [ ] Bucket `guiones` creado en Supabase
- [ ] Variables de entorno configuradas en `backend/.env`
- [ ] Tabla `guion_archivo` creada en la base de datos
- [ ] Backend corriendo sin errores
- [ ] Frontend corriendo sin errores
- [ ] Campaña telefónica existente en la base de datos
- [ ] Archivos de ejemplo descargados y listos para probar

---

**Fecha de implementación**: 2025-11-28  
**Versión**: 1.0.0
