package pe.unmsm.crm.marketing.campanas.telefonicas.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.*;
import pe.unmsm.crm.marketing.campanas.telefonicas.application.GuionArchivoService;
import pe.unmsm.crm.marketing.campanas.telefonicas.application.GuionService;
import pe.unmsm.crm.marketing.campanas.telefonicas.application.TelemarketingService;
import pe.unmsm.crm.marketing.shared.utils.ResponseUtils;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TelemarketingController {

    private final TelemarketingService service;
    private final GuionArchivoService guionArchivoService;
    private final GuionService guionService;

    // ==================== CAMPAÑAS ====================

    /**
     * 1. GET /agentes/{id}/campanias-telefonicas
     * Obtiene las campañas activas asignadas a un agente.
     */
    @GetMapping("/agentes/{id}/campanias-telefonicas")
    public ResponseEntity<Map<String, Object>> obtenerCampaniasPorAgente(@PathVariable Long id) {
        List<CampaniaTelefonicaDTO> campanias = service.obtenerCampaniasPorAgente(id);
        return ResponseUtils.success(campanias, "Campañas obtenidas exitosamente");
    }

    /**
     * 2. POST /campanias-telefonicas
     * Crea una nueva campaña telefónica.
     */
    @PostMapping("/campanias-telefonicas")
    public ResponseEntity<Map<String, Object>> crearCampania(
            @Valid @RequestBody CreateCampaniaTelefonicaRequest request) {
        CampaniaTelefonicaDTO campania = service.crearCampania(request);
        return ResponseUtils.success(campania, "Campaña creada exitosamente");
    }

    /**
     * 3. GET /campanias-telefonicas/{id}
     * Obtiene el detalle de una campaña específica.
     */
    @GetMapping("/campanias-telefonicas/{id}")
    public ResponseEntity<Map<String, Object>> obtenerCampania(@PathVariable Long id) {
        CampaniaTelefonicaDTO campania = service.obtenerCampaniaPorId(id);
        return ResponseUtils.success(campania, "Campaña obtenida exitosamente");
    }

    // ==================== CONTACTOS ====================

    /**
     * 4. GET /campanias-telefonicas/{id}/contactos
     * Obtiene la lista de contactos (leads) de una campaña.
     */
    @GetMapping("/campanias-telefonicas/{id}/contactos")
    public ResponseEntity<Map<String, Object>> obtenerContactosCampania(@PathVariable Long id) {
        List<ContactoDTO> contactos = service.obtenerContactosDeCampania(id);
        return ResponseUtils.success(contactos, "Contactos obtenidos exitosamente");
    }

    // ==================== COLA DE LLAMADAS ====================

    /**
     * 5. GET /campanias-telefonicas/{id}/cola
     * Obtiene la cola de llamadas pendientes para una campaña.
     */
    @GetMapping("/campanias-telefonicas/{id}/cola")
    public ResponseEntity<Map<String, Object>> obtenerColaLlamadas(@PathVariable Long id) {
        List<ContactoDTO> cola = service.obtenerCola(id);
        return ResponseUtils.success(cola, "Cola de llamadas obtenida exitosamente");
    }

    /**
     * 6. POST /campanias-telefonicas/{id}/cola/siguiente
     * Asigna y obtiene el siguiente contacto de la cola para un agente.
     */
    @PostMapping("/campanias-telefonicas/{id}/cola/siguiente")
    public ResponseEntity<Map<String, Object>> obtenerSiguienteContacto(
            @PathVariable Long id,
            @RequestBody Map<String, Long> request) {
        Long idAgente = request.get("idAgente");
        ContactoDTO contacto = service.obtenerSiguienteContacto(id, idAgente);
        return ResponseUtils.success(contacto, "Siguiente contacto asignado exitosamente");
    }

    /**
     * 7. POST /campanias-telefonicas/{id}/contactos/{idContacto}/tomar
     * Permite a un agente tomar un contacto específico de la lista.
     */
    @PostMapping("/campanias-telefonicas/{id}/contactos/{idContacto}/tomar")
    public ResponseEntity<Map<String, Object>> tomarContacto(
            @PathVariable Long id,
            @PathVariable Long idContacto,
            @RequestBody Map<String, Long> request) {
        Long idAgente = request.get("idAgente");
        ContactoDTO contacto = service.tomarContacto(id, idContacto, idAgente);
        return ResponseUtils.success(contacto, "Contacto asignado exitosamente");
    }

    /**
     * 8. POST /agentes/{idAgente}/campanias-telefonicas/{idCampania}/pausar-cola
     * Pausa la asignación automática de llamadas para un agente.
     */
    @PostMapping("/agentes/{idAgente}/campanias-telefonicas/{idCampania}/pausar-cola")
    public ResponseEntity<Map<String, Object>> pausarCola(
            @PathVariable Long idAgente,
            @PathVariable Long idCampania) {
        service.pausarCola(idAgente, idCampania);
        return ResponseUtils.success(null, "Cola pausada exitosamente");
    }

    /**
     * 9. POST /agentes/{idAgente}/campanias-telefonicas/{idCampania}/reanudar-cola
     * Reanuda la asignación automática de llamadas.
     */
    @PostMapping("/agentes/{idAgente}/campanias-telefonicas/{idCampania}/reanudar-cola")
    public ResponseEntity<Map<String, Object>> reanudarCola(
            @PathVariable Long idAgente,
            @PathVariable Long idCampania) {
        service.reanudarCola(idAgente, idCampania);
        return ResponseUtils.success(null, "Cola reanudada exitosamente");
    }

    /**
     * POST /public/v1/campanias-telefonicas/cola/urgente
     * Agrega un contacto urgente a la cola de una campaña con prioridad ALTA.
     * Endpoint público para integración con gestor de encuestas.
     * 
     * La campaña se determina automáticamente a partir de id_encuesta.
     */
    @PostMapping("/public/v1/campanias-telefonicas/cola/urgente")
    public ResponseEntity<Map<String, Object>> agregarContactoUrgente(
            @Valid @RequestBody AddUrgentContactRequest request) {
        try {
            ContactoDTO contacto = service.agregarContactoUrgente(request);
            return ResponseUtils.success(contacto, "Contacto agregado a cola con prioridad alta");
        } catch (IllegalArgumentException e) {
            return ResponseUtils.error(e.getMessage(), 400);
        }
    }

    // ==================== LLAMADAS ====================

    /**
     * GET /llamadas/{id}
     * Obtiene el detalle de una llamada específica.
     */
    @GetMapping("/llamadas/{id}")
    public ResponseEntity<Map<String, Object>> obtenerLlamada(@PathVariable Long id) {
        LlamadaDTO llamada = service.obtenerLlamada(id);
        return ResponseUtils.success(llamada, "Llamada obtenida exitosamente");
    }

    /**
     * 10. POST /campanias-telefonicas/{id}/llamadas/resultado
     * Registra el resultado de una llamada.
     */
    @PostMapping("/campanias-telefonicas/{idCampania}/llamadas/resultado")
    public ResponseEntity<Map<String, Object>> registrarResultadoLlamada(
            @PathVariable Long idCampania,
            @RequestParam Long idAgente,
            @Valid @RequestBody ResultadoLlamadaRequest request) {
        LlamadaDTO llamada = service.registrarResultadoLlamada(idCampania, idAgente, request);
        return ResponseUtils.success(llamada, "Resultado registrado exitosamente");
    }

    // ==================== HISTORIAL DE LLAMADAS ====================

    /**
     * 11. GET /campanias-telefonicas/{id}/llamadas
     * Obtiene el historial de llamadas de una campaña (para el agente).
     */
    @GetMapping("/campanias-telefonicas/{id}/llamadas")
    public ResponseEntity<Map<String, Object>> obtenerHistorialLlamadas(
            @PathVariable Long id,
            @RequestParam(required = false) Long idAgente) {
        List<LlamadaDTO> llamadas = service.obtenerHistorialLlamadas(id, idAgente);
        return ResponseUtils.success(llamadas, "Historial obtenido exitosamente");
    }

    // ==================== MÉTRICAS ====================

    /**
     * 12. GET /campanias-telefonicas/{id}/metricas/agentes/{idAgente}
     * Obtiene las métricas de un agente en una campaña específica.
     */
    @GetMapping("/campanias-telefonicas/{id}/metricas/agentes/{idAgente}")
    public ResponseEntity<Map<String, Object>> obtenerMetricasCampania(
            @PathVariable Long id,
            @PathVariable Long idAgente) {
        MetricasAgenteDTO metricas = service.obtenerMetricasAgente(id, idAgente);
        return ResponseUtils.success(metricas, "Métricas obtenidas exitosamente");
    }

    /**
     * 13. GET /agentes/{idAgente}/metricas-campania
     * Obtiene las métricas generales de todas las campanas del agente.
     */
    @GetMapping("/agentes/{idAgente}/metricas-campania")
    public ResponseEntity<Map<String, Object>> obtenerMetricasGeneralesAgente(
            @PathVariable Long idAgente) {
        // Usamos null para indicar métricas globales
        MetricasAgenteDTO metricas = service.obtenerMetricasAgente(null, idAgente);
        return ResponseUtils.success(metricas, "Métricas generales obtenidas exitosamente");
    }

    /**
     * GET /campanias-telefonicas/{id}/metricas-diarias
     * Obtiene las métricas diarias de una campaña para un agente (pendientes,
     * realizadas hoy, efectivas hoy).
     */
    @GetMapping("/campanias-telefonicas/{id}/metricas-diarias")
    public ResponseEntity<Map<String, Object>> obtenerMetricasDiarias(
            @PathVariable Long id,
            @RequestParam Long idAgente) {
        MetricasDiariasDTO metricas = service.obtenerMetricasDiarias(id, idAgente);
        return ResponseUtils.success(metricas, "Métricas diarias obtenidas exitosamente");
    }

    /**
     * GET /campanias-telefonicas/{id}/metricas
     * Obtiene métricas completas de una campaña
     */
    @GetMapping("/campanias-telefonicas/{id}/metricas")
    public ResponseEntity<Map<String, Object>> obtenerMetricasCampania(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "30") Integer dias) {
        MetricasCampaniaDTO metricas = service.obtenerMetricasCampania(id, dias);
        return ResponseUtils.success(metricas, "Métricas de campaña obtenidas exitosamente");
    }

    // ==================== GUIONES ====================

    /**
     * GET /campanias-telefonicas/{id}/guion
     * Obtiene el guion activo asociado a una campaña telefónica.
     */
    @GetMapping("/campanias-telefonicas/{id}/guion")
    public ResponseEntity<Map<String, Object>> obtenerGuionCampania(@PathVariable Long id) {
        try {
            GuionDTO guion = service.obtenerGuionDeCampania(id);
            return ResponseUtils.success(guion, "Guión obtenido exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al obtener guión: " + e.getMessage(), 500);
        }
    }

    // ==================== SESION DE GUION (MEMENTO) ====================

    @PostMapping("/llamadas/{idLlamada}/guion/sesion")
    public ResponseEntity<Map<String, Object>> guardarSesionGuion(
            @PathVariable Long idLlamada,
            @RequestParam Long idAgente,
            @RequestBody ScriptSessionRequest request) {
        ScriptSessionDTO dto = service.guardarSesionGuion(idLlamada, idAgente, request);
        return ResponseUtils.success(dto, "Sesión de guion guardada");
    }

    @GetMapping("/llamadas/{idLlamada}/guion/sesion")
    public ResponseEntity<Map<String, Object>> obtenerSesionGuion(
            @PathVariable Long idLlamada,
            @RequestParam Long idAgente) {
        ScriptSessionDTO dto = service.obtenerSesionGuion(idLlamada, idAgente);
        return ResponseUtils.success(dto, "Sesión de guion obtenida");
    }

    // ==================== GESTIÓN DE ARCHIVOS DE GUIONES ====================

    /**
     * POST /campanias-telefonicas/{id}/guiones/general
     * Sube un guión general para una campaña (solo archivos .md).
     */
    @PostMapping("/campanias-telefonicas/{id}/guiones/general")
    public ResponseEntity<Map<String, Object>> subirGuionGeneral(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false, defaultValue = "1") Long idUsuario) {
        try {
            GuionArchivoDTO guion = guionArchivoService.subirGuionGeneral(id, file, idUsuario);
            return ResponseUtils.success(guion, "Guión subido exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseUtils.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ResponseUtils.error("Error al subir guión: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /campanias-telefonicas/{id}/guiones/general
     * Lista los guiones generales de una campaña.
     */
    @GetMapping("/campanias-telefonicas/{id}/guiones/general")
    public ResponseEntity<Map<String, Object>> listarGuionesGenerales(@PathVariable Long id) {
        List<GuionArchivoDTO> guiones = guionArchivoService.listarGuionesGenerales(id);
        return ResponseUtils.success(guiones, "Guiones obtenidos exitosamente");
    }

    /**
     * POST /campanias-telefonicas/{id}/guiones/agente/{idAgente}
     * Sube un guión específico de un agente para una campaña (solo archivos .md).
     */
    @PostMapping("/campanias-telefonicas/{id}/guiones/agente/{idAgente}")
    public ResponseEntity<Map<String, Object>> subirGuionAgente(
            @PathVariable Long id,
            @PathVariable Long idAgente,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false, defaultValue = "1") Long idUsuario) {
        try {
            GuionArchivoDTO guion = guionArchivoService.subirGuionAgente(id, idAgente, file, idUsuario);
            return ResponseUtils.success(guion, "Guión subido exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseUtils.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ResponseUtils.error("Error al subir guión: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /campanias-telefonicas/{id}/guiones/agente/{idAgente}
     * Lista los guiones de un agente específico en una campaña.
     */
    @GetMapping("/campanias-telefonicas/{id}/guiones/agente/{idAgente}")
    public ResponseEntity<Map<String, Object>> listarGuionesAgente(
            @PathVariable Long id,
            @PathVariable Long idAgente) {
        List<GuionArchivoDTO> guiones = guionArchivoService.listarGuionesAgente(id, idAgente);
        return ResponseUtils.success(guiones, "Guiones obtenidos exitosamente");
    }

    /**
     * DELETE /guiones/{idGuion}
     * Elimina un guión (metadata y archivo físico).
     */
    @DeleteMapping("/guiones/{idGuion}")
    public ResponseEntity<Map<String, Object>> eliminarGuion(@PathVariable Integer idGuion) {
        try {
            guionArchivoService.eliminarGuion(idGuion);
            return ResponseUtils.success(null, "Guión eliminado exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al eliminar guión: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /guiones/{idGuion}/download
     * Descarga un archivo de guión.
     */
    @GetMapping("/guiones/{idGuion}/download")
    public ResponseEntity<byte[]> descargarGuion(@PathVariable Integer idGuion) {
        try {
            byte[] contenido = guionArchivoService.descargarGuion(idGuion);
            return ResponseEntity.ok()
                    .header("Content-Type", "text/markdown")
                    .header("Content-Disposition", "attachment; filename=\"guion.md\"")
                    .body(contenido);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * GET /guiones/{idGuion}/contenido
     * Obtiene el contenido markdown de un guión como texto.
     */
    @GetMapping("/guiones/{idGuion}/contenido")
    public ResponseEntity<Map<String, Object>> obtenerContenidoGuion(@PathVariable Integer idGuion) {
        try {
            String contenido = guionArchivoService.obtenerContenidoMarkdown(idGuion);
            return ResponseUtils.success(Map.of("contenido", contenido), "Contenido obtenido exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al obtener contenido: " + e.getMessage(), 500);
        }
    }

    // ==================== GESTIÓN DE GUIONES ESTRUCTURADOS ====================

    /**
     * POST /guiones
     * Crea un nuevo guión estructurado con secciones.
     */
    @PostMapping("/guiones")
    public ResponseEntity<Map<String, Object>> crearGuionEstructurado(
            @Valid @RequestBody CreateGuionRequest request) {
        try {
            GuionDTO guion = guionService.crearGuion(request);
            return ResponseUtils.success(guion, "Guión creado exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al crear guión: " + e.getMessage(), 500);
        }
    }

    /**
     * PUT /guiones/{id}
     * Actualiza un guión estructurado existente.
     */
    @PutMapping("/guiones/{id}")
    public ResponseEntity<Map<String, Object>> actualizarGuion(
            @PathVariable Integer id,
            @Valid @RequestBody CreateGuionRequest request) {
        try {
            GuionDTO guion = guionService.actualizarGuion(id, request);
            return ResponseUtils.success(guion, "Guión actualizado exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al actualizar guión: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /guiones/{id}
     * Obtiene un guión estructurado por ID con todas sus secciones.
     */
    @GetMapping("/guiones/{id}")
    public ResponseEntity<Map<String, Object>> obtenerGuionEstructurado(@PathVariable Integer id) {
        try {
            GuionDTO guion = guionService.obtenerGuionPorId(id);
            return ResponseUtils.success(guion, "Guión obtenido exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al obtener guión: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /guiones (actualizado)
     * Obtiene todos los guiones estructurados.
     * Nota: Este endpoint ahora devuelve guiones con estructura completa.
     */
    @GetMapping("/guiones-estructurados")
    public ResponseEntity<Map<String, Object>> listarGuionesEstructurados() {
        try {
            List<GuionDTO> guiones = guionService.listarGuiones();
            return ResponseUtils.success(guiones, "Guiones obtenidos exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al listar guiones: " + e.getMessage(), 500);
        }
    }

    /**
     * POST /campanias-telefonicas/{id}/vincular-guion
     * Vincula un guión estructurado a una campaña.
     */
    @PostMapping("/campanias-telefonicas/{id}/vincular-guion")
    public ResponseEntity<Map<String, Object>> vincularGuionACampania(
            @PathVariable("id") Long idCampania,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer idGuion = request.get("idGuion");
            if (idGuion == null) {
                return ResponseUtils.error("El idGuion es requerido", 400);
            }

            // TODO: Obtener usuario autenticado real
            Long idUsuario = 1L;

            GuionArchivoDTO guionArchivo = guionArchivoService.vincularGuionACampaña(idCampania, idGuion, idUsuario);
            return ResponseUtils.success(guionArchivo, "Guión vinculado exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al vincular guión: " + e.getMessage(), 500);
        }
    }
}
