# Plan de Implementación: Autenticación de Usuarios

Implementar un sistema completo de autenticación de usuarios con control de acceso basado en roles (Admin y Agente) a nivel de base de datos, backend y frontend.

## Contexto del Sistema Actual

### Base de Datos
- Tabla `agente_marketing` existente con campos básicos: `id_agente`, `nombre`, `email`, `telefono`, `activo`
- **No tiene credenciales** de autenticación (username/password)
- Tablas de asignación de camparas existentes:
  - `campania_agente` (campañas telefónicas)
  - `campanas_mailing` (campañas de mailing) con campo `id_agente_asignado`

### Backend
- **Spring Boot sin Spring Security configurado actualmente**
- No se encontraron:
  - Dependencia `spring-boot-starter-security` en pom.xml
  - Clases de configuración de seguridad
  - Anotaciones `@PreAuthorize`
- Dos entidades de agente similares en diferentes paquetes

### Frontend
- React Router sin protección de rutas
- Login template (`frontend/login.html`) ya existe con diseño completo
- Rutas clave identificadas:
  - `/leads` - Listado de leads
  - `/marketing/segmentacion` - Segmentación
  - `/encuestas` - Encuestas
  - `/marketing/campanas` - Gestor general de campañas
  - `/emailing` - Campañas de mailing
  - `/marketing/campanas/telefonicas` - Campañas telefónicas

## Roles y Permisos

### 🔴 Admin
- **Acceso total** a todas las funcionalidades del sistema

### 🟢 Agente 
- **Visualización y filtrado** en:
  - `/leads`
  - `/marketing/segmentacion`
  - `/encuestas`
- **Prohibido**:
  - Acceso a `/marketing/campanas` (gestor general)
  - Crear/editar campañas
  - Botones de acción se ocultarán
- **Condicional basado en asignación**:
  - Si asignado a campañas de **mailing**: acceso total a `/emailing` (no ve ícono de campañas telefónicas)
  - Si asignado a campañas **telefónicas**: acceso total a `/marketing/campanas/telefonicas` (no ve ícono de mailing)
  - Solo ve campañas **asignadas a él**

## Cambios Propuestos

### Base de Datos

#### [NEW] `usuarios`
```sql
CREATE TABLE usuarios (
  id_usuario BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL, -- BCrypt hash
  activo BIT(1) NOT NULL DEFAULT 1,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_usuario),
  INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

#### [NEW] `roles`
```sql
CREATE TABLE roles (
  id_rol INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(50) NOT NULL UNIQUE, -- 'ADMIN', 'AGENTE'
  descripcion VARCHAR(255),
  PRIMARY KEY (id_rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO roles (nombre, descripcion) VALUES 
('ADMIN', 'Acceso total al sistema'),
('AGENTE', 'Agente de marketing con permisos limitados');
```

#### [NEW] `usuario_rol`
```sql
CREATE TABLE usuario_rol (
  id_usuario BIGINT NOT NULL,
  id_rol INT NOT NULL,
  PRIMARY KEY (id_usuario, id_rol),
  CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
  CONSTRAINT fk_usuario_rol_rol FOREIGN KEY (id_rol) REFERENCES roles (id_rol) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

#### [MODIFY] `agente_marketing`
```sql
ALTER TABLE agente_marketing 
ADD COLUMN id_usuario BIGINT DEFAULT NULL AFTER id_agente,
ADD CONSTRAINT fk_agente_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE SET NULL;
```

---

### Backend (Spring Boot + Spring Security)

#### [NEW] `pom.xml` - Agregar dependencias
```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

#### Estructura de Paquetes
```
backend/src/main/java/pe/unmsm/crm/marketing/
├── security/
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   └── JwtConfig.java
│   ├── jwt/
│   │   ├── JwtTokenProvider.java
│   │   └── JwtAuthenticationFilter.java
│   ├── service/
│   │   ├── UserDetailsServiceImpl.java
│   │   └── AuthenticationService.java
│   ├── domain/
│   │   ├── UsuarioEntity.java
│   │   ├── RolEntity.java
│   │   └── UsuarioRolEntity.java
│   ├── repository/
│   │   ├── UsuarioRepository.java
│   │   └── RolRepository.java
│   └── api/
│       ├── AuthController.java
│       ├── dto/
│       │   ├── LoginRequest.java
│       │   ├── LoginResponse.java
│       │   └── UserInfoDTO.java
```

#### [NEW] [SecurityConfig.java](file:///c:/Users/marec/Desktop/Wankas_v2/backend/src/main/java/pe/unmsm/crm/marketing/security/config/SecurityConfig.java)
Configura Spring Security:
- Desactiva sesiones (stateless con JWT)
- Configura filtro JWT
- Define rutas públicas (`/api/auth/login`) y protegidas
- Habilita CORS

#### [NEW] [JwtTokenProvider.java](file:///c:/Users/marec/Desktop/Wankas_v2/backend/src/main/java/pe/unmsm/crm/marketing/security/jwt/JwtTokenProvider.java)
Genera y valida tokens JWT con:
- Secret key configurable via `application.properties`
- Expiración de 24 horas
- Claims personalizados (userId, roles)

#### [NEW] [JwtAuthenticationFilter.java](file:///c:/Users/marec/Desktop/Wankas_v2/backend/src/main/java/pe/unmsm/crm/marketing/security/jwt/JwtAuthenticationFilter.java)
Filtro que intercepta requests para extraer y validar JWT del header `Authorization: Bearer <token>`

#### [NEW] [UserDetailsServiceImpl.java](file:///c:/Users/marec/Desktop/Wankas_v2/backend/src/main/java/pe/unmsm/crm/marketing/security/service/UserDetailsServiceImpl.java)
Implementa `UserDetailsService` de Spring Security para cargar usuario y roles desde la base de datos

#### [NEW] [AuthController.java](file:///c:/Users/marec/Desktop/Wankas_v2/backend/src/main/java/pe/unmsm/crm/marketing/security/api/AuthController.java)
Endpoints REST:
- `POST /api/auth/login` - Autenticación y generación de JWT
- `GET /api/auth/me` - Información del usuario autenticado
- `POST /api/auth/logout` - Logout (invalida token si implementamos blacklist)

#### [NEW] Entidades JPA
- `UsuarioEntity` - Tabla `usuarios`
- `RolEntity` - Tabla `roles`  
- `UsuarioRolEntity` - Tabla `usuario_rol` (relación N:N)

#### [MODIFY] [AgenteMarketingEntity.java](file:///c:/Users/marec/Desktop/Wankas_v2/backend/src/main/java/pe/unmsm/crm/marketing/campanas/telefonicas/infra/jpa/entity/AgenteMarketingEntity.java)
```java
@ManyToOne
@JoinColumn(name = "id_usuario")
private UsuarioEntity usuario;
```

#### [NEW] Servicios de Autorización
Agregar métodos helper en servicios existentes para filtrar por agente:
- `CampaniaTelefonicaService.findByAgente(Integer idAgente)`
- `CampanasMailingService.findByAgente(Integer idAgente)`

#### [MODIFY] Controllers existentes
Agregar anotaciones de seguridad:
```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> createCampana(...) { ... }

@PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
public ResponseEntity<?> getLeads(...) { ... }
```

---

### Frontend (React + TypeScript)

#### [NEW] Context de Autenticación

##### [NEW] [AuthContext.tsx](file:///c:/Users/marec/Desktop/Wankas_v2/frontend/src/shared/context/AuthContext.tsx)
```tsx
interface AuthContextType {
  user: UserInfo | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
  hasRole: (role: string) => boolean;
  isLoading: boolean;
}
```

#### [NEW] Servicios API

##### [NEW] [auth.api.ts](file:///c:/Users/marec/Desktop/Wankas_v2/frontend/src/shared/services/auth.api.ts)
```tsx
export const authApi = {
  login: (username: string, password: string) => Promise<LoginResponse>,
  getMe: () => Promise<UserInfo>,
  logout: () => Promise<void>
};
```

#### [NEW] Componentes de Autenticación

##### [NEW] [LoginPage.tsx](file:///c:/Users/marec/Desktop/Wankas_v2/frontend/src/app/auth/LoginPage.tsx)
Componente React basado en `frontend/login.html` que:
- Renderiza el formulario de login
- Llama a `authApi.login()`
- Guarda token JWT en localStorage
- Redirige a `/leads` tras login exitoso

##### [NEW] [ProtectedRoute.tsx](file:///c:/Users/marec/Desktop/Wankas_v2/frontend/src/shared/components/routing/ProtectedRoute.tsx)
HOC que:
- Verifica si usuario está autenticado
- Opcionalmente verifica roles requeridos
- Redirige a `/login` si no está autenticado

##### [NEW] [RoleGuard.tsx](file:///c:/Users/marec/Desktop/Wankas_v2/frontend/src/shared/components/routing/RoleGuard.tsx)
Componente que oculta contenido si el usuario no tiene el rol:
```tsx
<RoleGuard requiredRole="ADMIN">
  <Button>Crear Campaña</Button>
</RoleGuard>
```

#### [MODIFY] [AppRouter.tsx](file:///c:/Users/marec/Desktop/Wankas_v2/frontend/src/app/AppRouter.tsx)
```tsx
<Routes>
  <Route path="/login" element={<LoginPage />} />
  <Route path="/" element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
    <Route index element={<Navigate to="/leads" replace />} />
    
    {/* Todos pueden ver leads (read-only para agentes) */}
    <Route path="leads" element={<LeadsListPage />} />
    
    {/* Solo ADMIN puede acceder al gestor de campañas */}
    <Route path="marketing/campanas" element={
      <ProtectedRoute requiredRole="ADMIN">
        <CampanasListPage />
      </ProtectedRoute>
    } />
    
    {/* Acceso condicional basado en asignación */}
    <Route path="emailing/*" element={<MailingRoutes />} />
    <Route path="marketing/campanas/telefonicas/*" element={<TelemarketingRoutes />} />
  </Route>
</Routes>
```

#### [MODIFY] Páginas existentes

##### [MODIFY] [LeadsListPage.tsx](file:///c:/Users/marec/Desktop/Wankas_v2/frontend/src/modules/marketing/leads/pages/LeadsListPage.tsx)
```tsx
const { hasRole } = useAuth();

// Ocultar botones de acción para agentes
{hasRole('ADMIN') && (
  <Button onClick={handleCreate}>Crear Lead</Button>
)}
```

##### [MODIFY] [Sidebar.tsx](file:///c:/Users/marec/Desktop/Wankas_v2/frontend/src/shared/components/layout/Sidebar.tsx)
```tsx
// Mostrar íconos condicionalmente basado en rol y asignaciones
const showMailingIcon = hasRole('ADMIN') || isAssignedToMailing;
const showPhoneIcon = hasRole('ADMIN') || isAssignedToPhone;
```

#### [NEW] Interceptor HTTP
Axios interceptor para agregar JWT a todos los requests:
```tsx
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('jwt_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

#### [NEW] Manejo de Errores 401/403
Interceptor de respuesta para redirigir a `/login` en caso de token inválido/expirado

---

## Plan de Verificación

### 1. Verificación de Base de Datos
**Manual**:
1. Conectarse a la base de datos MySQL
2. Ejecutar:
   ```sql
   SHOW TABLES LIKE 'usuarios';
   SHOW TABLES LIKE 'roles';
   SELECT * FROM roles;
   ```
3. Verificar que las tablas existen y los roles están insertados

### 2. Verificación de Backend - Login

**Manual con cURL**:
```bash
# Test 1: Login exitoso
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
# Esperado: 200 OK con JWT token

# Test 2: Login fallido
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrong"}'
# Esperado: 401 Unauthorized

# Test 3: Acceso protegido sin token
curl http://localhost:8080/api/v1/leads
# Esperado: 401 Unauthorized

# Test 4: Acceso protegido con token
curl http://localhost:8080/api/v1/leads \
  -H "Authorization: Bearer <TOKEN_FROM_TEST1>"
# Esperado: 200 OK con lista de leads
```

### 3. Verificación de Autorización Backend

**Manual con cURL**:
```bash
# Test 1: ADMIN puede crear campaña
curl -X POST http://localhost:8080/api/v1/marketing/campanas \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Test",...}'
# Esperado: 201 Created

# Test 2: AGENTE NO puede crear campaña
curl -X POST http://localhost:8080/api/v1/marketing/campanas \
  -H "Authorization: Bearer <AGENTE_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Test",...}'
# Expected: 403 Forbidden
```

### 4. Verificación de Frontend - Login Flow

**Manual en navegador** (usuario ejecuta):
1. Iniciar backend: `cd backend && ./mvnw spring-boot:run`
2. Iniciar frontend: `cd frontend && npm run dev`
3. Navegar a `http://localhost:5173`
4. Verificar redirección automática a `/login`
5. Ingresar credenciales de admin (username: `admin`, password: `admin123`)
6. Verificar redirección a `/leads` tras login
7. Verificar que token JWT está en localStorage (DevTools → Application → Local Storage)
8. Refrescar página y verificar que sesión persiste
9. Hacer logout y verificar redirección a `/login`

### 5. Verificación de Permisos de Agente

**Manual en navegador** (usuario ejecuta):
1. Login como agente (username: `agente1`, password: `agente123`)
2. Verificar visualización de:
   - `/leads` - puede ver listado pero **botones de crear/editar ocultos**
   - `/marketing/segmentacion` - puede ver pero **botones de acción ocultos**
   - `/encuestas` - puede ver pero **botones de crear nueva encuesta ocultos**
3. Intentar navegar manualmente a `/marketing/campanas`
   - Esperado: Redirección a página de acceso denegado o `/leads`
4. Si agente asignado a mailing: verificar que puede acceder a `/emailing`
5. Si agente asignado a telefónicas: verificar que puede acceder a `/marketing/campanas/telefonicas`
6. Verificar en Sidebar que solo aparecen íconos de módulos a los que tiene acceso

### 6. Verificación de Asignación de Campañas

**Manual en navegador** (usuario ejecuta):
1. Login como `agente1` (asignado a campaña telefónica ID=3)
2. Navegar a `/marketing/campanas/telefonicas`
3. Verificar que **solo** aparece la campaña con ID=3
4. Login como `admin`
5. Navegar a `/marketing/campanas/telefonicas`
6. Verificar que aparecen **todas** las campañas

### 7. Verificación de Seguridad de Tokens

**Manual**:
1. Login exitoso y copiar JWT token
2. Modificar manualmente el token en localStorage (agregar caracteres random)
3. Intentar hacer un request a cualquier endpoint protegido
4. Verificar que el backend responde 401 Unauthorized
5. Verificar que el frontend redirige automáticamente a `/login`

---

## Notas de Implementación

### Seguridad
- Contraseñas hasheadas con BCrypt (factor 12)
- Tokens JWT con expiración de 24h
- Secret key debe estar en variables de entorno en producción
- No almacenar información sensible en JWT payload

### Datos de Prueba
Crear usuarios iniciales vía SQL:
```sql
-- Password: admin123
INSERT INTO usuarios (username, password_hash, activo) VALUES 
('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyg28jJuDCWW', 1);

-- Password: agente123  
INSERT INTO usuarios (username, password_hash, activo) VALUES
('agente1', '$2a$12$KIXQhRIxOzlJj9qLwXxHKe5dXJZPZsG7FPtUxQ6HJ.Z1YhjPKCNJm', 1);

INSERT INTO usuario_rol VALUES (1, 1); -- admin tiene rol ADMIN
INSERT INTO usuario_rol VALUES (2, 2); -- agente1 tiene rol AGENTE

-- Vincular agente1 con el registro de agente_marketing id=1
UPDATE agente_marketing SET id_usuario = 2 WHERE id_agente = 1;
```

### Consideraciones de Performance
- Cachear roles de usuario en JWT para evitar consultas repetidas a DB
- Implementar refresh tokens si tokens de 24h son muy largos
- Considerar Redis para blacklist de tokens (logout efectivo)
