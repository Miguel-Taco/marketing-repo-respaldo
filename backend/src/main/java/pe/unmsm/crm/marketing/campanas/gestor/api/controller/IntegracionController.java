package pe.unmsm.crm.marketing.campanas.gestor.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.request.FinalizarEjecucionRequest;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.response.ContextoEjecucionResponse;
import pe.unmsm.crm.marketing.campanas.gestor.application.mapper.CampanaMapper;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.input.IGestorCampanaUseCase;
import pe.unmsm.crm.marketing.shared.infra.exception.BusinessException;

/**
 * Controlador REST para webhooks e integración con módulos de ejecución
 * (Mailing y Llamadas).
 */
@RestController
@RequestMapping("/api/v1/campanas")
@RequiredArgsConstructor
@Slf4j
public class IntegracionController {

    private final IGestorCampanaUseCase gestorCampanaUseCase;
    private final CampanaMapper mapper;

    /**
     * GET /api/v1/campanas/{id}/contexto_ejecucion
     * Endpoint para que Mailing/Llamadas obtengan los datos necesarios para
     * ejecutar
     */
    @GetMapping("/{id}/contexto_ejecucion")
    public ResponseEntity<ContextoEjecucionResponse> obtenerContextoEjecucion(@PathVariable Long id) {
        Campana campana = gestorCampanaUseCase.obtenerPorId(id);

        // Validar que esté en estado Programada o Vigente
        String estado = campana.getEstado().getNombre();
        if (!"Programada".equals(estado) && !"Vigente".equals(estado)) {
            throw new BusinessException(
                    "INVALID_STATE", "Solo se puede obtener el contexto de campanas Programadas o Vigentes. Estado actual: " + estado);
        }

        ContextoEjecucionResponse contexto = mapper.toContextoResponse(campana);
        return ResponseEntity.ok(contexto);
    }

    /**
     * POST /api/v1/campanas/{id}/finalizar_webhook
     * Webhook llamado por Mailing/Llamadas para notificar fin de ejecución
     */
    @PostMapping("/{id}/finalizar_webhook")
    public ResponseEntity<Void> finalizarCampanaWebhook(
            @PathVariable Long id,
            @Valid @RequestBody FinalizarEjecucionRequest request) {

        log.info("Webhook recibido para finalizar campaña {}. Resultado: {}, Mensaje: {}",
                id, request.getResultado(), request.getMensaje());

        // Validar que la campaña esté Vigente
        Campana campana = gestorCampanaUseCase.obtenerPorId(id);
        String estado = campana.getEstado().getNombre();

        if (!"Vigente".equals(estado)) {
            log.warn("Intento de finalizar campaña {} que no está Vigente. Estado: {}", id, estado);
            throw new BusinessException("INVALID_STATE", "Solo se pueden finalizar campanas en estado Vigente");
        }

        // Finalizar campaña
        gestorCampanaUseCase.finalizar(id);

        log.info("Campaña {} finalizada exitosamente vía webhook", id);
        return ResponseEntity.ok().build();
    }
}
