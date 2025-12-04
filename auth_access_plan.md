# Plan de Implementación - Autenticación y Control de Accesos

## 1. Modelo y perfil de usuario
- **Mapear columnas faltantes** en `AgenteMarketingEntity`/`AgenteEntity` (`id_usuario`, `canal_principal`, etc.) y crear un repositorio/servicio para obtener el agente asociado a un `Usuario`.
- **Extender los DTOs**: nuevo `UserProfileDTO` con `userId`, `username`, `roles`, `idAgente`, `canalPrincipal`, `campañasMailingAsignadas`, `campañasTelefonoAsignadas`, flags (`puedeMailing`, `puedeTelefonia`).
- **Actualizar datos base** (`marketing.sql` o migraciones) para garantizar relaciones Usuario ↔ Agente ↔ Campaña según el modelo.

## 2. API de autenticación
- **Modificar `AuthenticationService`** y controladores (`/api/auth/login`, `/api/auth/me`) para devolver el `UserProfileDTO` junto con el token.
- **Incluir claims adicionales** en el JWT (idUsuario, idAgente, módulos autorizados) o garantizar que `getCurrentUser` reconstruya el perfil completo desde BD.
- **Sincronizar `UserInfoDTO`/`LoginResponse`** para exponer la nueva estructura al frontend.

## 3. Reglas de acceso en backend
- **Crear utilitarios de contexto** (`@CurrentUser`, `UserPrincipal`) para acceder al usuario autenticado y su agente dentro de los controladores.
- **Agregar restricciones por ruta**: usar `@PreAuthorize` o reglas en `SecurityConfig` para que solo `ROLE_ADMIN` acceda a `/api/v1/campanas/**`, y que módulos como `/api/v1/mailing/**` o `/api/v1/agentes/**` validen rol y canal.
- **Eliminar parámetros `idAgente` del request** y tomar el valor desde el contexto autenticado. Validar que el agente esté asignado a la campaña antes de responder (mailing y telemarketing).
- **Corregir repositorios y servicios** (`CampaniaTelefonicaRepository`, `JpaCampaignDataProvider`, `CampanaMailingService`, etc.) para filtrar campañas por asignaciones reales (`CampaniaAgente`, `Campanas_mailing.id_agente_asignado`) y devolver 403 cuando el agente no tenga acceso.

## 4. Frontend - contexto y permisos
- **Actualizar `AuthContext` y tipos (`UserInfo`)** para almacenar el perfil extendido (roles, idAgente, flags de módulos, campañas permitidas).
- **Persistir el perfil en login** y refrescarlo con `/auth/me`; exponer helpers `hasRole`, `canAccessMailing`, `canAccessTelefonia`.

## 5. Frontend - navegación y rutas
- **Extender `ProtectedRoute`** para aceptar requisitos por rol y/o módulo (ej. `requiredModule="MAILING"`).
- **Actualizar el `Sidebar` y menús**: mostrar Emailing o Teléfono solo si el usuario tiene acceso; ocultar `/marketing/campanas` para agentes.
- **Redirecciones**: si un agente intenta acceder a un módulo no autorizado, enviarlo a `/leads` (o vista por defecto) y mostrar mensaje.

## 6. Frontend - módulos operativos
- [HECHO] **Eliminar IDs quemados** y usar el `idAgente` del `AuthContext` en contexts/APIs de mailing y telemarketing.
- [HECHO] **Aplicar filtros adicionales en UI** para garantizar que solo se muestren campañas asignadas; ocultar/inhabilitar botones de creación/edición según rol/canal.
- [HECHO] **Mostrar mensajes "sin autorización"** coherentes cuando falte asignación.

## 7. Verificación y QA
- **Agregar pruebas unitarias/integración** que cubran login, `/auth/me`, telemarketing/mailing protegidos y reglas de autorización (mock de usuarios admin vs agente).
- **Preparar datos seed** con al menos: Admin, Agente Mailing y Agente Telefónico con campañas distintas.
- **Documentar escenarios de QA** (checklist por rol) y actualizar guías para desarrolladores sobre cómo crear usuarios/agentes y asignar campañas.
