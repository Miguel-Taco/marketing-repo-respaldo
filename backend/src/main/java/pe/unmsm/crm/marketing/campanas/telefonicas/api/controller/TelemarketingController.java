package pe.unmsm.crm.marketing.campanas.telefonicas.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.*;
import pe.unmsm.crm.marketing.campanas.telefonicas.application.GuionArchivoService;
import pe.unmsm.crm.marketing.campanas.telefonicas.application.GuionService;
import pe.unmsm.crm.marketing.campanas.telefonicas.application.TelemarketingService;
import pe.unmsm.crm.marketing.campanas.telefonicas.application.service.GrabacionService;
import pe.unmsm.crm.marketing.security.service.UserAuthorizationService;
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
    private final GrabacionService grabacionService;
    private final UserAuthorizationService userAuthorizationService;

    // ==================== CAMPAÃƒâ€˜AS ====================

    @GetMapping("/agentes/me/campanias-telefonicas")
    public ResponseEntity<Map<String, Object>> obtenerCampaniasAgenteActual() {
        if (userAuthorizationService.isAdmin()) {
            List<CampaniaTelefonicaDTO> campanias = service.obtenerTodasLasCampanias();
            return ResponseUtils.success(campanias, "Campañas obtenidas exitosamente");
        }
        Long currentAgent = requireCurrentAgent();
        List<CampaniaTelefonicaDTO> campanias = service.obtenerCampaniasPorAgente(currentAgent);
        return ResponseUtils.success(campanias, "Campañas obtenidas exitosamente");
    }

    /**
     * 2. POST /campanias-telefonicas
     * Crea una nueva campaÃƒÂ±a telefÃƒÂ³nica.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/campanias-telefonicas")
    public ResponseEntity<Map<String, Object>> crearCampania(
            @Valid @RequestBody CreateCampaniaTelefonicaRequest request) {
        CampaniaTelefonicaDTO campania = service.crearCampania(request);
        return ResponseUtils.success(campania, "CampaÃƒÂ±a creada exitosamente");
    }

    /**
     * 3. GET /campanias-telefonicas/{id}
     * Obtiene el detalle de una campaÃƒÂ±a especÃƒÂ­fica.
     */
    @GetMapping("/campanias-telefonicas/{id}")
    public ResponseEntity<Map<String, Object>> obtenerCampania(@PathVariable Long id) {
        requireCampaniaAccess(id);
        CampaniaTelefonicaDTO campania = service.obtenerCampaniaPorId(id);
        return ResponseUtils.success(campania, "CampaÃƒÂ±a obtenida exitosamente");
    }

    // ==================== CONTACTOS ====================

    /**
     * 4. GET /campanias-telefonicas/{id}/contactos
     * Obtiene la lista de contactos (leads) de una campaÃƒÂ±a.
     */
    @GetMapping("/campanias-telefonicas/{id}/contactos")
    public ResponseEntity<Map<String, Object>> obtenerContactosCampania(@PathVariable Long id) {
        requireCampaniaAccess(id);
        List<ContactoDTO> contactos = service.obtenerContactosDeCampania(id);
        return ResponseUtils.success(contactos, "Contactos obtenidos exitosamente");
    }

    // ==================== COLA DE LLAMADAS ====================

    /**
     * 5. GET /campanias-telefonicas/{id}/cola
     * Obtiene la cola de llamadas pendientes para una campaÃƒÂ±a.
     */
    @GetMapping("/campanias-telefonicas/{id}/cola")
    public ResponseEntity<Map<String, Object>> obtenerColaLlamadas(@PathVariable Long id) {
        requireCampaniaAccess(id);
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
        requireCampaniaAccess(id);
        Long resolvedAgente = resolveAgentOrCurrent(request.get("idAgente"));
        ContactoDTO contacto = service.obtenerSiguienteContacto(id, resolvedAgente);
        return ResponseUtils.success(contacto, "Siguiente contacto asignado exitosamente");
    }

    /**
     * 7. POST /campanias-telefonicas/{id}/contactos/{idContacto}/tomar
     * Permite a un agente tomar un contacto especÃƒÂ­fico de la lista.
     */
    @PostMapping("/campanias-telefonicas/{id}/contactos/{idContacto}/tomar")
    public ResponseEntity<Map<String, Object>> tomarContacto(
            @PathVariable Long id,
            @PathVariable Long idContacto,
            @RequestBody Map<String, Long> request) {
        requireCampaniaAccess(id);
        Long resolvedAgente = resolveAgentOrCurrent(request.get("idAgente"));
        ContactoDTO contacto = service.tomarContacto(id, idContacto, resolvedAgente);
        return ResponseUtils.success(contacto, "Contacto asignado exitosamente");
    }

    @PostMapping("/campanias-telefonicas/{idCampania}/pausar-cola")
    public ResponseEntity<Map<String, Object>> pausarColaActual(
            @PathVariable Long idCampania) {
        Long currentAgent = requireCurrentAgent();
        requireCampaniaAccess(idCampania);
        service.pausarCola(currentAgent, idCampania);
        return ResponseUtils.success(null, "Cola pausada exitosamente");
    }

    @PostMapping("/campanias-telefonicas/{idCampania}/reanudar-cola")
    public ResponseEntity<Map<String, Object>> reanudarColaActual(
            @PathVariable Long idCampania) {
        Long currentAgent = requireCurrentAgent();
        requireCampaniaAccess(idCampania);
        service.reanudarCola(currentAgent, idCampania);
        return ResponseUtils.success(null, "Cola reanudada exitosamente");
    }

    /**
     * GET /agentes/me/llamadas-programadas
     * Obtiene las llamadas programadas del agente actual
     */
    @GetMapping("/agentes/me/llamadas-programadas")
    public ResponseEntity<Map<String, Object>> obtenerLlamadasProgramadas() {
        if (userAuthorizationService.isAdmin()) {
            try {
                Long currentAgent = requireCurrentAgent();
                List<ContactoDTO> llamadas = service.obtenerLlamadasProgramadas(currentAgent);
                return ResponseUtils.success(llamadas, "Llamadas programadas obtenidas exitosamente");
            } catch (AccessDeniedException e) {
                // Admin sin agente -> Devolver TODAS las llamadas programadas
                List<ContactoDTO> llamadas = service.obtenerLlamadasProgramadas(null);
                return ResponseUtils.success(llamadas, "Todas las llamadas programadas");
            }
        }
        Long currentAgent = requireCurrentAgent();
        List<ContactoDTO> llamadas = service.obtenerLlamadasProgramadas(currentAgent);
        return ResponseUtils.success(llamadas, "Llamadas programadas obtenidas exitosamente");
    }

    /**
     * POST /public/v1/campanias-telefonicas/cola/urgente
     * Agrega un contacto urgente a la cola de una campaÃƒÂ±a con prioridad ALTA.
     * Endpoint pÃƒÂºblico para integraciÃƒÂ³n con gestor de encuestas.
     * 
     * La campaÃƒÂ±a se determina automÃƒÂ¡ticamente a partir de id_encuesta.
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
     * Obtiene el detalle de una llamada especÃƒÂ­fica.
     */
    @GetMapping("/llamadas/{id}")
    public ResponseEntity<Map<String, Object>> obtenerLlamada(@PathVariable Long id) {
        LlamadaDTO llamada = service.obtenerLlamada(id);
        ensureLlamadaAccess(llamada);
        return ResponseUtils.success(llamada, "Llamada obtenida exitosamente");
    }

    /**
     * 10. POST /campanias-telefonicas/{id}/llamadas/resultado
     * Registra el resultado de una llamada.
     */
    @PostMapping("/campanias-telefonicas/{idCampania}/llamadas/resultado")
    public ResponseEntity<Map<String, Object>> registrarResultadoLlamada(
            @PathVariable Long idCampania,
            @RequestParam(required = false) Long idAgente,
            @Valid @RequestBody ResultadoLlamadaRequest request) {
        requireCampaniaAccess(idCampania);
        Long resolvedAgente = resolveAgentOrCurrent(idAgente);
        LlamadaDTO llamada = service.registrarResultadoLlamada(idCampania, resolvedAgente, request);
        return ResponseUtils.success(llamada, "Resultado registrado exitosamente");
    }

    // ==================== GRABACIONES DE LLAMADAS ====================

    /**
     * POST /campanias-telefonicas/{idCampania}/grabaciones
     * Sube una grabación de llamada
     */
    @PostMapping("/campanias-telefonicas/{idCampania}/grabaciones")
    public ResponseEntity<Map<String, Object>> subirGrabacion(
            @PathVariable Long idCampania,
            @RequestParam("archivo") org.springframework.web.multipart.MultipartFile archivo,
            @RequestParam Long idLead,
            @RequestParam(required = false) Integer idLlamada,
            @RequestParam Integer duracionSegundos,
            @RequestParam(required = false) String resultado) {
        try {
            requireCampaniaAccess(idCampania);
            Long currentAgent = requireCurrentAgent();

            SubirGrabacionRequest request = new SubirGrabacionRequest();
            request.setIdCampania(idCampania.intValue());
            request.setIdLead(idLead);
            request.setIdLlamada(idLlamada);
            request.setDuracionSegundos(duracionSegundos);
            request.setResultado(resultado);
            request.setArchivo(archivo);

            GrabacionDTO grabacion = grabacionService.procesarGrabacion(request, currentAgent.intValue());
            return ResponseUtils.success(grabacion, "Grabación procesada exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseUtils.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ResponseUtils.error("Error al procesar grabación: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /agentes/me/grabaciones
     * Lista las grabaciones del agente actual con filtros
     * Admins pueden ver todas las grabaciones (sin filtro de agente)
     */
    @GetMapping("/agentes/me/grabaciones")
    public ResponseEntity<Map<String, Object>> listarMisGrabaciones(
            @RequestParam(required = false) Integer idCampania,
            @RequestParam(required = false) String resultado,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime fechaDesde,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime fechaHasta,
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Integer currentAgent = null;
            if (!userAuthorizationService.isAdmin()) {
                currentAgent = requireCurrentAgent().intValue();
            }
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page,
                    size);

            org.springframework.data.domain.Page<GrabacionDTO> grabaciones = grabacionService.listarGrabaciones(
                    currentAgent,
                    idCampania,
                    resultado,
                    fechaDesde,
                    fechaHasta,
                    busqueda,
                    pageable);

            return ResponseUtils.success(grabaciones, "Grabaciones obtenidas exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al listar grabaciones: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /grabaciones/{idGrabacion}
     * Obtiene los detalles de una grabación específica
     * Admins pueden acceder a cualquier grabación
     */
    @GetMapping("/grabaciones/{idGrabacion}")
    public ResponseEntity<Map<String, Object>> obtenerGrabacion(
            @PathVariable Long idGrabacion) {
        try {
            Integer currentAgent = null;
            if (!userAuthorizationService.isAdmin()) {
                currentAgent = requireCurrentAgent().intValue();
            }
            GrabacionDTO grabacion = grabacionService.obtenerGrabacion(idGrabacion, currentAgent);
            return ResponseUtils.success(grabacion, "Grabación obtenida exitosamente");
        } catch (RuntimeException e) {
            return ResponseUtils.error(e.getMessage(), 404);
        } catch (Exception e) {
            return ResponseUtils.error("Error al obtener grabación: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /grabaciones/{idGrabacion}/audio
     * Obtiene la URL firmada temporal para reproducir el audio
     * Admins pueden acceder a cualquier grabación
     */
    @GetMapping("/grabaciones/{idGrabacion}/audio")
    public ResponseEntity<Map<String, Object>> obtenerAudioUrl(
            @PathVariable Long idGrabacion) {
        try {
            Integer currentAgent = null;
            if (!userAuthorizationService.isAdmin()) {
                currentAgent = requireCurrentAgent().intValue();
            }
            String audioUrl = grabacionService.obtenerAudioUrl(idGrabacion, currentAgent);
            return ResponseUtils.success(Map.of("url", audioUrl), "URL de audio generada exitosamente");
        } catch (RuntimeException e) {
            return ResponseUtils.error(e.getMessage(), 404);
        } catch (Exception e) {
            return ResponseUtils.error("Error al obtener URL de audio: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /grabaciones/{idGrabacion}/transcripcion
     * Obtiene la transcripción en formato markdown
     * Admins pueden acceder a cualquier grabación
     */
    @GetMapping("/grabaciones/{idGrabacion}/transcripcion")
    public ResponseEntity<String> obtenerTranscripcion(
            @PathVariable Long idGrabacion) {
        try {
            Integer currentAgent = null;
            if (!userAuthorizationService.isAdmin()) {
                currentAgent = requireCurrentAgent().intValue();
            }
            String transcripcion = grabacionService.obtenerTranscripcion(idGrabacion, currentAgent);
            return ResponseEntity.ok()
                    .header("Content-Type", "text/markdown; charset=UTF-8")
                    .body(transcripcion);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Transcripción no encontrada: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al obtener transcripción: " + e.getMessage());
        }
    }

    /**
     * DELETE /grabaciones/{idGrabacion}
     * Elimina una grabación (audio y transcripción)
     * Admins pueden eliminar cualquier grabación
     */
    @DeleteMapping("/grabaciones/{idGrabacion}")
    public ResponseEntity<Map<String, Object>> eliminarGrabacion(
            @PathVariable Long idGrabacion) {
        try {
            Integer currentAgent = null;
            if (!userAuthorizationService.isAdmin()) {
                currentAgent = requireCurrentAgent().intValue();
            }
            grabacionService.eliminarGrabacion(idGrabacion, currentAgent);
            return ResponseUtils.success(null, "Grabación elimin ada exitosamente");
        } catch (RuntimeException e) {
            return ResponseUtils.error(e.getMessage(), 404);
        } catch (Exception e) {
            return ResponseUtils.error("Error al eliminar grabación: " + e.getMessage(), 500);
        }
    }

    // ==================== HISTORIAL DE LLAMADAS ====================

    /**
     * 11. GET /campanias-telefonicas/{id}/llamadas
     * Obtiene el historial de llamadas de una campaÃƒÂ±a (para el agente).
     */
    @GetMapping("/campanias-telefonicas/{id}/llamadas")
    public ResponseEntity<Map<String, Object>> obtenerHistorialLlamadas(
            @PathVariable Long id,
            @RequestParam(required = false) Long idAgente) {
        requireCampaniaAccess(id);
        Long resolvedAgente = resolveAgentForQuery(idAgente);
        List<LlamadaDTO> llamadas = service.obtenerHistorialLlamadas(id, resolvedAgente);
        return ResponseUtils.success(llamadas, "Historial obtenido exitosamente");
    }

    /**
     * GET /campanias-telefonicas/{idCampania}/llamadas/{idLlamada}/encuesta
     * Obtiene los detalles del envÃƒÂ­o de encuesta para una llamada especÃƒÂ­fica.
     */
    @GetMapping("/campanias-telefonicas/{idCampania}/llamadas/{idLlamada}/encuesta")
    public ResponseEntity<Map<String, Object>> obtenerDetalleEncuesta(
            @PathVariable Long idCampania,
            @PathVariable Integer idLlamada) {
        try {
            requireCampaniaAccess(idCampania);
            EnvioEncuestaDTO detalle = service.obtenerDetalleEncuesta(idLlamada);
            if (detalle == null) {
                return ResponseUtils.error("No se encontrÃƒÂ³ envÃƒÂ­o de encuesta para esta llamada", 404);
            }
            return ResponseUtils.success(detalle, "Detalle de encuesta obtenido exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al obtener detalle de encuesta: " + e.getMessage(), 500);
        }
    }

    // ==================== MÃƒâ€°TRICAS ====================

    /**
     * 12. GET /campanias-telefonicas/{id}/metricas/agente
     * Obtiene las m??tricas del agente autenticado dentro de una campa??a.
     */
    @GetMapping("/campanias-telefonicas/{id}/metricas/agente")
    public ResponseEntity<Map<String, Object>> obtenerMetricasCampaniaAgenteActual(
            @PathVariable Long id) {
        requireCampaniaAccess(id);
        Long currentAgent = requireCurrentAgent();
        MetricasAgenteDTO metricas = service.obtenerMetricasAgente(id, currentAgent);
        return ResponseUtils.success(metricas, "M??tricas obtenidas exitosamente");
    }

    /**
     * GET /campanias-telefonicas/{id}/metricas-diarias
     * Obtiene las mÃƒÂ©tricas diarias de una campaÃƒÂ±a para un agente (pendientes,
     * realizadas hoy, efectivas hoy).
     */

    @GetMapping("/agentes/me/metricas-campania")
    public ResponseEntity<Map<String, Object>> obtenerMetricasGeneralesAgenteActual() {
        if (userAuthorizationService.isAdmin()) {
            MetricasAgenteDTO metricas = service.obtenerMetricasAgente(null, null);
            return ResponseUtils.success(metricas, "Métricas generales obtenidas exitosamente");
        }
        Long currentAgent = requireCurrentAgent();
        MetricasAgenteDTO metricas = service.obtenerMetricasAgente(null, currentAgent);
        return ResponseUtils.success(metricas, "Métricas generales obtenidas exitosamente");
    }

    @GetMapping("/campanias-telefonicas/{id}/metricas-diarias")
    public ResponseEntity<Map<String, Object>> obtenerMetricasDiarias(
            @PathVariable Long id,
            @RequestParam(required = false) Long idAgente) {
        requireCampaniaAccess(id);
        Long resolvedAgente = resolveAgentOrCurrent(idAgente);
        MetricasDiariasDTO metricas = service.obtenerMetricasDiarias(id, resolvedAgente);
        return ResponseUtils.success(metricas, "MÃƒÂ©tricas diarias obtenidas exitosamente");
    }

    /**
     * GET /campanias-telefonicas/{id}/metricas
     * Obtiene mÃƒÂ©tricas completas de una campaÃƒÂ±a
     */
    @GetMapping("/campanias-telefonicas/{id}/metricas")
    public ResponseEntity<Map<String, Object>> obtenerMetricasCampania(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "30") Integer dias) {
        requireCampaniaAccess(id);
        MetricasCampaniaDTO metricas = service.obtenerMetricasCampania(id, dias);
        return ResponseUtils.success(metricas, "MÃƒÂ©tricas de campaÃƒÂ±a obtenidas exitosamente");
    }

    // ==================== GUIONES ====================

    /**
     * GET /campanias-telefonicas/{id}/guion
     * Obtiene el guion activo asociado a una campaÃƒÂ±a telefÃƒÂ³nica.
     */
    @GetMapping("/campanias-telefonicas/{id}/guion")
    public ResponseEntity<Map<String, Object>> obtenerGuionCampania(@PathVariable Long id) {
        try {
            requireCampaniaAccess(id);
            GuionDTO guion = service.obtenerGuionDeCampania(id);
            return ResponseUtils.success(guion, "GuiÃƒÂ³n obtenido exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al obtener guiÃƒÂ³n: " + e.getMessage(), 500);
        }
    }

    // ==================== SESION DE GUION (MEMENTO) ====================

    @PostMapping("/llamadas/{idLlamada}/guion/sesion")
    public ResponseEntity<Map<String, Object>> guardarSesionGuion(
            @PathVariable Long idLlamada,
            @RequestParam(required = false) Long idAgente,
            @RequestBody ScriptSessionRequest request) {
        Long resolvedAgente = resolveAgentOrCurrent(idAgente);
        ScriptSessionDTO dto = service.guardarSesionGuion(idLlamada, resolvedAgente, request);
        return ResponseUtils.success(dto, "SesiÃƒÂ³n de guion guardada");
    }

    @GetMapping("/llamadas/{idLlamada}/guion/sesion")
    public ResponseEntity<Map<String, Object>> obtenerSesionGuion(
            @PathVariable Long idLlamada,
            @RequestParam(required = false) Long idAgente) {
        Long resolvedAgente = resolveAgentOrCurrent(idAgente);
        ScriptSessionDTO dto = service.obtenerSesionGuion(idLlamada, resolvedAgente);
        return ResponseUtils.success(dto, "SesiÃƒÂ³n de guion obtenida");
    }

    // ==================== GESTIÃƒâ€œN DE ARCHIVOS DE GUIONES ====================

    /**
     * POST /campanias-telefonicas/{id}/guiones/general
     * Sube un guiÃƒÂ³n general para una campaÃƒÂ±a (solo archivos .md).
     */
    @PostMapping("/campanias-telefonicas/{id}/guiones/general")
    public ResponseEntity<Map<String, Object>> subirGuionGeneral(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false, defaultValue = "1") Long idUsuario) {
        try {
            requireCampaniaAccess(id);
            GuionArchivoDTO guion = guionArchivoService.subirGuionGeneral(id, file, idUsuario);
            return ResponseUtils.success(guion, "GuiÃƒÂ³n subido exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseUtils.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ResponseUtils.error("Error al subir guiÃƒÂ³n: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /campanias-telefonicas/{id}/guiones/general
     * Lista los guiones generales de una campaÃƒÂ±a.
     */
    @GetMapping("/campanias-telefonicas/{id}/guiones/general")
    public ResponseEntity<Map<String, Object>> listarGuionesGenerales(@PathVariable Long id) {
        requireCampaniaAccess(id);
        List<GuionArchivoDTO> guiones = guionArchivoService.listarGuionesGenerales(id);
        return ResponseUtils.success(guiones, "Guiones obtenidos exitosamente");
    }

    /**
     * POST /campanias-telefonicas/{id}/guiones/agente/{idAgente}
     * Sube un guiÃƒÂ³n especÃƒÂ­fico de un agente para una campaÃƒÂ±a (solo
     * archivos .md).
     */
    @PostMapping("/campanias-telefonicas/{id}/guiones/agente/{idAgente}")
    public ResponseEntity<Map<String, Object>> subirGuionAgente(
            @PathVariable Long id,
            @PathVariable Long idAgente,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false, defaultValue = "1") Long idUsuario) {
        try {
            requireCampaniaAccess(id);
            Long resolvedAgente = resolvePathAgent(idAgente);
            GuionArchivoDTO guion = guionArchivoService.subirGuionAgente(id, resolvedAgente, file, idUsuario);
            return ResponseUtils.success(guion, "GuiÃƒÂ³n subido exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseUtils.error(e.getMessage(), 400);
        } catch (Exception e) {
            return ResponseUtils.error("Error al subir guiÃƒÂ³n: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /campanias-telefonicas/{id}/guiones/agente/{idAgente}
     * Lista los guiones de un agente especÃƒÂ­fico en una campaÃƒÂ±a.
     */
    @GetMapping("/campanias-telefonicas/{id}/guiones/agente/{idAgente}")
    public ResponseEntity<Map<String, Object>> listarGuionesAgente(
            @PathVariable Long id,
            @PathVariable Long idAgente) {
        requireCampaniaAccess(id);
        Long resolvedAgente = resolvePathAgent(idAgente);
        List<GuionArchivoDTO> guiones = guionArchivoService.listarGuionesAgente(id, resolvedAgente);
        return ResponseUtils.success(guiones, "Guiones obtenidos exitosamente");
    }

    /**
     * DELETE /guiones/{idGuion}
     * Elimina un guiÃƒÂ³n (metadata y archivo fÃƒÂ­sico).
     */
    @DeleteMapping("/guiones/{idGuion}")
    public ResponseEntity<Map<String, Object>> eliminarGuion(@PathVariable Integer idGuion) {
        try {
            guionArchivoService.eliminarGuion(idGuion);
            return ResponseUtils.success(null, "GuiÃƒÂ³n eliminado exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al eliminar guiÃƒÂ³n: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /guiones/{idGuion}/download
     * Descarga un archivo de guiÃƒÂ³n.
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
     * Obtiene el contenido markdown de un guiÃƒÂ³n como texto.
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

    // ==================== GESTIÃƒâ€œN DE GUIONES ESTRUCTURADOS
    // ====================

    /**
     * POST /guiones
     * Crea un nuevo guiÃƒÂ³n estructurado con secciones.
     */
    @PostMapping("/guiones")
    public ResponseEntity<Map<String, Object>> crearGuionEstructurado(
            @Valid @RequestBody CreateGuionRequest request) {
        try {
            GuionDTO guion = guionService.crearGuion(request);
            return ResponseUtils.success(guion, "GuiÃƒÂ³n creado exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al crear guiÃƒÂ³n: " + e.getMessage(), 500);
        }
    }

    /**
     * PUT /guiones/{id}
     * Actualiza un guiÃƒÂ³n estructurado existente.
     */
    @PutMapping("/guiones/{id}")
    public ResponseEntity<Map<String, Object>> actualizarGuion(
            @PathVariable Integer id,
            @Valid @RequestBody CreateGuionRequest request) {
        try {
            GuionDTO guion = guionService.actualizarGuion(id, request);
            return ResponseUtils.success(guion, "GuiÃƒÂ³n actualizado exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al actualizar guiÃƒÂ³n: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /guiones/{id}
     * Obtiene un guiÃƒÂ³n estructurado por ID con todas sus secciones.
     */
    @GetMapping("/guiones/{id}")
    public ResponseEntity<Map<String, Object>> obtenerGuionEstructurado(@PathVariable Integer id) {
        try {
            GuionDTO guion = guionService.obtenerGuionPorId(id);
            return ResponseUtils.success(guion, "GuiÃƒÂ³n obtenido exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al obtener guiÃƒÂ³n: " + e.getMessage(), 500);
        }
    }

    /**
     * GET /campanias-telefonicas/{id}/guiones-estructurados
     * Lista los guiones estructurados de una campaña específica.
     */
    @GetMapping("/campanias-telefonicas/{id}/guiones-estructurados")
    public ResponseEntity<Map<String, Object>> listarGuionesPorCampania(@PathVariable Long id) {
        try {
            requireCampaniaAccess(id);
            List<GuionDTO> guiones = guionService.listarGuionesPorCampania(id);
            return ResponseUtils.success(guiones, "Guiones obtenidos exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al listar guiones: " + e.getMessage(), 500);
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
            requireCampaniaAccess(idCampania);
            Integer idGuion = request.get("idGuion");
            if (idGuion == null) {
                return ResponseUtils.error("El idGuion es requerido", 400);
            }

            // TODO: Obtener usuario autenticado real
            Long idUsuario = 1L;

            GuionArchivoDTO guionArchivo = guionArchivoService.vincularGuionACampania(idCampania, idGuion, idUsuario);
            return ResponseUtils.success(guionArchivo, "Guión vinculado exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al vincular guión: " + e.getMessage(), 500);
        }
    }

    // ==================== CONFIGURACIÓN ====================

    /**
     * GET /campanias-telefonicas/{id}/config
     * Obtiene la configuración de una campaña telefónica.
     */
    @GetMapping("/campanias-telefonicas/{id}/config")
    public ResponseEntity<Map<String, Object>> obtenerConfiguracion(@PathVariable Long id) {
        try {
            requireCampaniaAccess(id);
            CampaniaTelefonicaConfigDTO config = service.obtenerConfiguracion(id);
            return ResponseUtils.success(config, "Configuración obtenida exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al obtener configuración: " + e.getMessage(), 500);
        }
    }

    /**
     * PUT /campanias-telefonicas/{id}/config
     * Actualiza la configuración de una campaña telefónica.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/campanias-telefonicas/{id}/config")
    public ResponseEntity<Map<String, Object>> actualizarConfiguracion(
            @PathVariable Long id,
            @Valid @RequestBody CampaniaTelefonicaConfigDTO config) {
        try {
            requireCampaniaAccess(id);
            CampaniaTelefonicaConfigDTO updated = service.actualizarConfiguracion(id, config);
            return ResponseUtils.success(updated, "Configuración actualizada exitosamente");
        } catch (Exception e) {
            return ResponseUtils.error("Error al actualizar configuración: " + e.getMessage(), 500);
        }
    }

    // ==================== HELPER METHODS ====================

    private void requireCampaniaAccess(Long idCampania) {
        userAuthorizationService.ensureCampaniaTelefonicaAccess(idCampania);
    }

    private void ensureLlamadaAccess(LlamadaDTO llamada) {
        if (llamada == null) {
            throw new AccessDeniedException("No se pudo verificar la llamada solicitada");
        }
        Long campaniaId = llamada.getIdCampania();
        if (campaniaId != null) {
            requireCampaniaAccess(campaniaId);
            return;
        }
        Long agenteId = llamada.getIdAgente();
        if (agenteId != null) {
            requireAgentAccess(agenteId);
            return;
        }
        throw new AccessDeniedException("La llamada no contiene datos suficientes para valid ar acceso");
    }

    private Long requireAgentAccess(Long idAgente) {
        return userAuthorizationService.ensureAgentAccess(idAgente).longValue();
    }

    private Long resolvePathAgent(Long idAgente) {
        if (userAuthorizationService.isAdmin()) {
            return requireAgentAccess(idAgente);
        }
        return requireCurrentAgent();
    }

    private Long resolveAgentOrCurrent(Long idAgente) {
        if (idAgente == null) {
            if (userAuthorizationService.isAdmin()) {
                return null;
            }
            return requireCurrentAgent();
        }
        return requireAgentAccess(idAgente);
    }

    private Long resolveAgentForQuery(Long idAgente) {
        if (idAgente == null) {
            if (userAuthorizationService.isAdmin()) {
                return null;
            }
            return requireCurrentAgent();
        }
        return requireAgentAccess(idAgente);
    }

    private Long requireCurrentAgent() {
        return userAuthorizationService.requireCurrentAgentId().longValue();
    }
}
