package pe.unmsm.crm.marketing.campanas.mailing.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.*;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.response.*;
import pe.unmsm.crm.marketing.campanas.mailing.application.mapper.MailingMapper;
import pe.unmsm.crm.marketing.campanas.mailing.application.service.CampanaMailingService;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.CampanaMailing;
import pe.unmsm.crm.marketing.security.service.UserAuthorizationService;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/mailing")
@RequiredArgsConstructor
public class MailingController {

    private final CampanaMailingService service;
    private final MailingMapper mapper;
    private final UserAuthorizationService userAuthorizationService;

    // ============ HANDOFF: Recibe campaña del Gestor ============
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/campañas")
    public ResponseEntity<CampanaMailingResponse> crearCampana(
            @Valid @RequestBody CrearCampanaMailingRequest req) {
        CampanaMailing c = service.crearCampana(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(c));
    }

    // ============ PANEL: Listar campañas por estado ============
    @PreAuthorize("hasAnyRole('ADMIN','AGENTE')")
    @GetMapping("/campañas")
    public ResponseEntity<List<CampanaMailingResponse>> listarCampanas(
            @RequestParam(required = false) String estado) {

        Integer targetAgente = userAuthorizationService.isAdmin()
                ? null
                : userAuthorizationService.requireCurrentAgentId();

        List<Integer> permitidas = userAuthorizationService.loadMailingCampaignIds(targetAgente);
        List<CampanaMailing> campanas = switch (estado != null ? estado.toLowerCase() : "") {
            case "" -> service.listarTodas(permitidas);
            case "pendiente" -> service.listarPendientes(permitidas);
            case "listo" -> service.listarListos(permitidas);
            case "enviado" -> service.listarEnviados(permitidas);
            case "finalizado" -> service.listarFinalizados(permitidas);
            default -> service.listarTodas(permitidas);
        };
        
        List<CampanaMailingResponse> result = campanas.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    // ============ PANEL: Detalle de campaña ============
    @GetMapping("/campañas/{id}")
    public ResponseEntity<CampanaMailingResponse> obtenerDetalle(@PathVariable Integer id) {
        userAuthorizationService.ensureCampaniaMailingAccess(id);
        CampanaMailing c = service.obtenerDetalle(id);
        return ResponseEntity.ok(mapper.toResponse(c));
    }

    // ============ PANEL: Guardar borrador ============
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/campañas/{id}/preparacion")
    public ResponseEntity<Void> guardarBorrador(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarContenidoRequest req) {
        userAuthorizationService.ensureCampaniaMailingAccess(id);
        service.guardarBorrador(id, req);
        return ResponseEntity.ok().build();
    }

    // ============ PANEL: Marcar como listo ============
    @PutMapping("/campañas/{id}/estado")
    public ResponseEntity<Void> marcarListo(@PathVariable Integer id) {
        userAuthorizationService.ensureCampaniaMailingAccess(id);
        service.marcarListo(id);
        return ResponseEntity.ok().build();
    }

    // ============ PANEL: Obtener métricas ============
    @GetMapping("/campañas/{id}/metricas")
    public ResponseEntity<MetricasMailingResponse> obtenerMetricas(@PathVariable Integer id) {
        userAuthorizationService.ensureCampaniaMailingAccess(id);
        MetricasMailingResponse m = service.obtenerMetricas(id);
        return ResponseEntity.ok(m);
    }

    // ============ Cancelar campaña desde Gestor ============
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/campañas/gestion/{idCampanaGestion}/cancelar")
    public ResponseEntity<Void> cancelarPorGestor(@PathVariable Long idCampanaGestion) {
        service.cancelarPorGestor(idCampanaGestion);
        return ResponseEntity.ok().build();
    }
}
