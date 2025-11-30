package pe.unmsm.crm.marketing.campanas.mailing.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.*;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.response.*;
import pe.unmsm.crm.marketing.campanas.mailing.application.mapper.MailingMapper;
import pe.unmsm.crm.marketing.campanas.mailing.application.service.CampanaMailingService;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.CampanaMailing;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/mailing")
@RequiredArgsConstructor
public class MailingController {

    private final CampanaMailingService service;
    private final MailingMapper mapper;

    // ============ HANDOFF: Recibe campaña del Gestor ============
    @PostMapping("/campañas")
    public ResponseEntity<CampanaMailingResponse> crearCampana(
            @Valid @RequestBody CrearCampanaMailingRequest req) {
        CampanaMailing c = service.crearCampana(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(c));
    }

    // ============ PANEL: Listar campañas por estado ============
    @GetMapping("/campañas")
    public ResponseEntity<List<CampanaMailingResponse>> listarCampanas(
            @RequestParam(required = true) Integer idAgente,
            @RequestParam(required = false) String estado) {
        
        List<CampanaMailing> campanas;
        
        if (estado == null || estado.isEmpty()) {
            campanas = service.listarTodas(idAgente);
        } else {
            switch(estado.toLowerCase()) {
                case "pendiente":
                    campanas = service.listarPendientes(idAgente);
                    break;
                case "listo":
                    campanas = service.listarListos(idAgente);
                    break;
                case "enviado":
                    campanas = service.listarEnviados(idAgente);
                    break;
                case "finalizado":
                    campanas = service.listarFinalizados(idAgente);
                    break;
                default:
                    campanas = service.listarTodas(idAgente);
            }
        }
        
        List<CampanaMailingResponse> result = campanas.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    // ============ PANEL: Detalle de campaña ============
    @GetMapping("/campañas/{id}")
    public ResponseEntity<CampanaMailingResponse> obtenerDetalle(@PathVariable Integer id) {
        CampanaMailing c = service.obtenerDetalle(id);
        return ResponseEntity.ok(mapper.toResponse(c));
    }

    // ============ PANEL: Guardar borrador ============
    @PutMapping("/campañas/{id}/preparacion")
    public ResponseEntity<Void> guardarBorrador(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarContenidoRequest req) {
        service.guardarBorrador(id, req);
        return ResponseEntity.ok().build();
    }

    // ============ PANEL: Marcar como listo ============
    @PutMapping("/campañas/{id}/estado")
    public ResponseEntity<Void> marcarListo(@PathVariable Integer id) {
        service.marcarListo(id);
        return ResponseEntity.ok().build();
    }

    // ============ PANEL: Obtener métricas ============
    @GetMapping("/campañas/{id}/metricas")
    public ResponseEntity<MetricasMailingResponse> obtenerMetricas(@PathVariable Integer id) {
        MetricasMailingResponse m = service.obtenerMetricas(id);
        return ResponseEntity.ok(m);
    }
}