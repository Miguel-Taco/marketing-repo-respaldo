package pe.unmsm.crm.marketing.campanas.telefonicas.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.*;
import pe.unmsm.crm.marketing.campanas.telefonicas.application.TelemarketingService;
import pe.unmsm.crm.marketing.shared.utils.ResponseUtils;

import java.util.List;
import java.util.Map;

/**
 * Controller para campanas telefónicas (Telemarketing).
 * Implementa los 15 endpoints definidos más el endpoint de creación.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class TelemarketingController {

    private final TelemarketingService service;

    // ==================== CAMPAÑAS ====================

    /**
     * 1. GET /agentes/{idAgente}/campanias-telefonicas
     * Obtiene las campanas telefónicas asignadas a un agente.
     */
    @GetMapping("/agentes/{idAgente}/campanias-telefonicas")
    public ResponseEntity<Map<String, Object>> obtenerCampaniasAgente(@PathVariable Long idAgente) {
        List<CampaniaTelefonicaDTO> campanias = service.obtenerCampaniasPorAgente(idAgente);
        return ResponseUtils.success(campanias, "Campañas obtenidas exitosamente");
    }

    /**
     * POST /v1/campanias-telefonicas
     * Crea una nueva campaña telefónica (desde el gestor de campanas).
     */
    @PostMapping("/campanias-telefonicas")
    public ResponseEntity<Map<String, Object>> crearCampaniaTelefonica(
            @Valid @RequestBody CreateCampaniaTelefonicaRequest request) {
        CampaniaTelefonicaDTO campania = service.crearCampania(request);
        return ResponseUtils.success(campania, "Campaña creada exitosamente");
    }

    // ==================== CONTACTOS ====================

    /**
     * 2. GET /campanias-telefonicas/{id}/contactos
     * Obtiene los contactos/leads de una campaña telefónica.
     */
    @GetMapping("/campanias-telefonicas/{id}/contactos")
    public ResponseEntity<Map<String, Object>> obtenerContactosCampania(@PathVariable Long id) {
        List<ContactoDTO> contactos = service.obtenerContactosDeCampania(id);
        return ResponseUtils.success(contactos, "Contactos obtenidos exitosamente");
    }

    // ==================== COLA ====================

    /**
     * 3. GET /campanias-telefonicas/{id}/cola
     * Obtiene la cola de llamadas pendientes de una campaña.
     */
    @GetMapping("/campanias-telefonicas/{id}/cola")
    public ResponseEntity<Map<String, Object>> obtenerCola(@PathVariable Long id) {
        List<ContactoDTO> cola = service.obtenerCola(id);
        return ResponseUtils.success(cola, "Cola obtenida exitosamente");
    }

    /**
     * 4. POST /campanias-telefonicas/{id}/cola/siguiente
     * Obtiene el siguiente contacto automáticamente de la cola.
     */
    @PostMapping("/campanias-telefonicas/{id}/cola/siguiente")
    public ResponseEntity<Map<String, Object>> obtenerSiguienteContacto(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        Long idAgente = body.get("idAgente");
        ContactoDTO contacto = service.obtenerSiguienteContacto(id, idAgente);
        return ResponseUtils.success(contacto, "Siguiente contacto obtenido");
    }

    /**
     * 5. POST /campanias-telefonicas/{id}/contactos/{idContacto}/tomar
     * Toma manualmente un contacto específico de la cola.
     */
    @PostMapping("/campanias-telefonicas/{id}/contactos/{idContacto}/tomar")
    public ResponseEntity<Map<String, Object>> tomarContacto(
            @PathVariable Long id,
            @PathVariable Long idContacto,
            @RequestBody Map<String, Long> body) {
        Long idAgente = body.get("idAgente");
        ContactoDTO contacto = service.tomarContacto(id, idContacto, idAgente);
        return ResponseUtils.success(contacto, "Contacto tomado exitosamente");
    }

    /**
     * 6. POST /agentes/{idAgente}/campanias-telefonicas/{id}/pausar-cola
     * Pausa la cola de llamadas para el agente en una campaña.
     */
    @PostMapping("/agentes/{idAgente}/campanias-telefonicas/{id}/pausar-cola")
    public ResponseEntity<Map<String, Object>> pausarCola(
            @PathVariable Long idAgente,
            @PathVariable Long id) {
        service.pausarCola(idAgente, id);
        return ResponseUtils.success(null, "Cola pausada exitosamente");
    }

    /**
     * 7. POST /agentes/{idAgente}/campanias-telefonicas/{id}/reanudar-cola
     * Reanuda la cola de llamadas para el agente en una campaña.
     */
    @PostMapping("/agentes/{idAgente}/campanias-telefonicas/{id}/reanudar-cola")
    public ResponseEntity<Map<String, Object>> reanudarCola(
            @PathVariable Long idAgente,
            @PathVariable Long id) {
        service.reanudarCola(idAgente, id);
        return ResponseUtils.success(null, "Cola reanudada exitosamente");
    }

    // ==================== PANTALLA DE LLAMADA ====================

    /**
     * 8. GET /llamadas/{idLlamada}
     * Obtiene el detalle de una llamada específica.
     */
    @GetMapping("/llamadas/{idLlamada}")
    public ResponseEntity<Map<String, Object>> obtenerLlamada(@PathVariable Long idLlamada) {
        LlamadaDTO llamada = service.obtenerLlamada(idLlamada);
        return ResponseUtils.success(llamada, "Llamada obtenida exitosamente");
    }

    /**
     * 9. GET /campanias-telefonicas/{id}/guion
     * Obtiene el guion de llamada asociado a una campaña.
     */
    @GetMapping("/campanias-telefonicas/{id}/guion")
    public ResponseEntity<Map<String, Object>> obtenerGuion(@PathVariable Long id) {
        GuionDTO guion = service.obtenerGuionDeCampania(id);
        return ResponseUtils.success(guion, "Guion obtenido exitosamente");
    }

    /**
     * GET /guiones
     * Obtiene todos los guiones disponibles.
     */
    @GetMapping("/guiones")
    public ResponseEntity<Map<String, Object>> listarGuiones() {
        List<GuionDTO> guiones = service.listarTodosLosGuiones();
        return ResponseUtils.success(guiones, "Guiones obtenidos exitosamente");
    }

    // ==================== RESULTADO DE LLAMADA ====================

    /**
     * 10. POST /llamadas/{idLlamada}/resultado
     * Registra el resultado de una llamada (nota: usamos idCampania
     * indirectamente).
     */
    @PostMapping("/campanias-telefonicas/{idCampania}/llamadas/resultado")
    public ResponseEntity<Map<String, Object>> registrarResultado(
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
            @RequestParam Long idAgente) {
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
}
