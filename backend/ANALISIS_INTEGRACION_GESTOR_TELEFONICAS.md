# Análisis de Integración: Gestor de Campañas ↔ Campañas Telefónicas

## Fecha
2025-12-01

## Problemas Identificados

### 1. Campañas Canceladas Aparecen en el Panel Principal
**Ubicación del problema:**
- **Backend:** `CampaniaTelefonicaRepository.findByAgenteId()` (líneas 20-23)
- **Comportamiento actual:** Retorna TODAS las campañas donde `esArchivado = false`
- **Problema:** Incluye campañas con estado "Cancelada", "Pausada", "Finalizada", etc.

**Código actual:**
```java
@Query("SELECT c FROM CampaniaTelefonicaEntity c " +
       "WHERE c.esArchivado = false " +
       "ORDER BY c.fechaInicio DESC")
List<CampaniaTelefonicaEntity> findByAgenteId();
```

### 2. Se Puede Acceder a Campañas Pausadas
**Ubicación del problema:**
- **Frontend:** `CampaignsPage.tsx` - Botón "Entrar" (línea 172)
- **Frontend:** `CallQueuePage.tsx` - No valida estado de campaña
- **Comportamiento actual:** Al hacer clic en "Entrar", se navega sin restricciones
- **Problema:** Permite acceder a la cola, métricas e historial de campañas Pausadas, Canceladas o Finalizadas

**Código actual (CampaignsPage.tsx):**
```tsx
<button
    className="rounded-full bg-primary px-4 py-2 text-xs font-bold text-white hover:bg-primary/90"
    onClick={() => navigate(`/marketing/campanas/telefonicas/campanias/${campania.id}/cola`)}
>
    Entrar
</button>
```

## Soluciones Propuestas (Solo Modificaciones en Módulo Telefónico)

### Solución 1: Filtrar Campañas Canceladas del Listado

#### Backend
**Modificar:** `CampaniaTelefonicaRepository.findByAgenteId()`

**Cambio propuesto:**
```java
@Query("SELECT c FROM CampaniaTelefonicaEntity c " +
       "WHERE c.esArchivado = false " +
       "AND c.estado NOT IN ('Cancelada', 'Finalizada') " +
       "ORDER BY c.fechaInicio DESC")
List<CampaniaTelefonicaEntity> findByAgenteId();
```

**Justificación:**
- Las campañas "Canceladas" y "Finalizadas" no deben aparecer en el panel principal
- Los agentes solo deben ver campañas en estados: "Programada", "Vigente", "Pausada"

### Solución 2: Restringir Acceso a Campañas Pausadas

#### Frontend
**Modificar:** `CampaignsPage.tsx`

**Cambio propuesto (líneas 169-176):**
```tsx
const canEnterCampaign = (estado: string) => {
    return estado === 'Vigente' || estado === 'ACTIVA';
};

<button
    className={`rounded-full px-4 py-2 text-xs font-bold ${
        canEnterCampaign(campania.estado) 
            ? 'bg-primary text-white hover:bg-primary/90' 
            : 'bg-gray-300 text-gray-500 cursor-not-allowed'
    }`}
    onClick={() => {
        if (canEnterCampaign(campania.estado)) {
            navigate(`/marketing/campanas/telefonicas/campanias/${campania.id}/cola`);
        }
    }}
    disabled={!canEnterCampaign(campania.estado)}
>
    {canEnterCampaign(campania.estado) ? 'Entrar' : 'No disponible'}
</button>
```

**Modificar:** `CallQueuePage.tsx`

**Agregar validación de estado (después de línea 42):**
```tsx
const [campaignStatus, setCampaignStatus] = useState<string | null>(null);

useEffect(() => {
    if (id) {
        loadCampaignStatus();
    }
}, [id]);

const loadCampaignStatus = async () => {
    try {
        const campaign = await telemarketingApi.getCampania(Number(id));
        setCampaignStatus(campaign.estado);
        
        // Redirigir si la campaña no está activa
        if (campaign.estado !== 'Vigente' && campaign.estado !== 'ACTIVA') {
            navigate('/marketing/campanas/telefonicas', {
                state: { 
                    message: `No puedes acceder a esta campaña porque está en estado: ${campaign.estado}` 
                }
            });
        }
    } catch (error) {
        console.error('Error loading campaign status:', error);
    }
};
```

## Cambios en el Gestor (NO IMPLEMENTAR - SOLO DOCUMENTACIÓN)

### ⚠️ ATENCIÓN: Limitación Identificada

El `ProcesadorLlamadas` en el Gestor **NO** tiene métodos para notificar el archivado de campañas.

**Métodos actuales en ProcesadorLlamadas:**
- ✅ `programarCampana`
- ✅ `activarCampana`
- ✅ `notificarPausa`
- ✅ `notificarCancelacion`
- ✅ `notificarReanudacion`
- ✅ `reprogramarCampana`
- ❌ **FALTA:** `notificarArchivo` / `archivarCampana`

**Impacto:**
Si el Gestor archiva una campaña, el módulo de Campañas Telefónicas **NO recibirá notificación** y la campaña podría seguir visible (dependiendo del filtro).

**Solución recomendada (GESTOR - Fuera de alcance):**
```java
// En ProcesadorLlamadas.java (GESTOR)
public void notificarArchivo(Long idCampana) {
    log.info("Archivando campaña telefónica ID: {}", idCampana);
    facadeService.archivarCampania(idCampana);
}

// En CanalEjecucionRouter.java (GESTOR)
@Override
public void notificarArchivo(Long idCampana) {
    Campana campana = obtenerCampana(idCampana);
    if (campana.getCanalEjecucion() == CanalEjecucion.Llamadas) {
        procesadorLlamadas.notificarArchivo(idCampana);
    }
}
```

## Resumen de Implementación

### ✅ Cambios en Campañas Telefónicas (IMPLEMENTAR)
1. Modificar query `findByAgenteId()` para excluir Canceladas/Finalizadas
2. Agregar validación en `CampaignsPage.tsx` para deshabilitar botón "Entrar"
3. Agregar guard en `CallQueuePage.tsx` para redirigir si campaña no está activa
4. Aplicar validaciones similares en:
   - `CampaignMetricsPage.tsx`
   - `CampaignLeadsPage.tsx`
   - `CallHistoryPage.tsx`

### ⚠️ Cambios Requeridos en Gestor (INFORMAR AL USUARIO)
1. Agregar método `notificarArchivo` en `ProcesadorLlamadas`
2. Agregar método `notificarArchivo` en `ICanalEjecucionPort`
3. Implementar método en `CanalEjecucionRouter`
4. Agregar método `archivarCampania` en `CampaniaTelefonicaFacadeService` (ya existe `finalizarCampania`, sería similar)

## Mapeo de Estados

| Estado Gestor | Estado Telefónicas | Visible en Lista | Acceso Permitido |
|---------------|-------------------|------------------|------------------|
| Borrador | Programada / BORRADOR | ❌ No | ❌ No |
| Programada | Programada | ✅ Sí | ❌ No |
| Vigente | Vigente / ACTIVA | ✅ Sí | ✅ Sí |
| Pausada | Pausada / PAUSADA | ✅ Sí | ❌ No |
| Cancelada | Cancelada | ❌ No | ❌ No |
| Finalizada | Finalizada | ❌ No | ❌ No |
| Archivada | esArchivado=true | ❌ No | ❌ No |

## Conclusión

La integración actual tiene dos gaps principales:
1. No filtra campañas canceladas/finalizadas del listado
2. No valida el estado al acceder a funcionalidades de la campaña

Ambos problemas **pueden resolverse modificando solo el módulo de Campañas Telefónicas**, aunque se recomienda implementar el método `notificarArchivo` en el Gestor para completar la integración del ciclo de vida completo.
