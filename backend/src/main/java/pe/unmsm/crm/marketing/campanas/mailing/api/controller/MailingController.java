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
@RequestMapping("/api/mailing/v1")
@RequiredArgsConstructor
public class MailingController {

    private final CampanaMailingService service;
    private final MailingMapper mapper;

    // Handoff: Recibe campaña del Gestor
    @PostMapping("/campañas")
    public ResponseEntity<CampanaMailingResponse> crearCampana(@Valid @RequestBody CrearCampanaMailingRequest req) {
        CampanaMailing c = service.crearCampana(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(c));
    }

    // Panel: Listar pendientes
    @GetMapping("/campañas?estado=pendiente")
    public ResponseEntity<List<CampanaMailingResponse>> listarPendientes(@RequestParam Integer idAgente) {
        List<CampanaMailingResponse> result = service.listarPendientes(idAgente)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // Panel: Listar listos
    @GetMapping("/campañas?estado=listo")
    public ResponseEntity<List<CampanaMailingResponse>> listarListos(@RequestParam Integer idAgente) {
        List<CampanaMailingResponse> result = service.listarListos(idAgente)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // Panel: Listar enviados
    @GetMapping("/campañas?estado=enviado")
    public ResponseEntity<List<CampanaMailingResponse>> listarEnviados(@RequestParam Integer idAgente) {
        List<CampanaMailingResponse> result = service.listarEnviados(idAgente)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // Panel: Listar finalizados
    @GetMapping("/campañas?estado=finalizado")
    public ResponseEntity<List<CampanaMailingResponse>> listarFinalizados(@RequestParam Integer idAgente) {
        List<CampanaMailingResponse> result = service.listarFinalizados(idAgente)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // Panel: Detalle
    @GetMapping("/campañas/{id}")
    public ResponseEntity<CampanaMailingResponse> obtenerDetalle(@PathVariable Integer id) {
        CampanaMailing c = service.obtenerDetalle(id);
        return ResponseEntity.ok(mapper.toResponse(c));
    }

    // Panel: Guardar borrador
    @PutMapping("/campañas/{id}/preparacion")
    public ResponseEntity<Void> guardarBorrador(@PathVariable Integer id, @Valid @RequestBody ActualizarContenidoRequest req) {
        service.guardarBorrador(id, req);
        return ResponseEntity.ok().build();
    }

    // Panel: Marcar como listo
    @PutMapping("/campañas/{id}/estado")
    public ResponseEntity<Void> marcarListo(@PathVariable Integer id) {
        service.marcarListo(id);
        return ResponseEntity.ok().build();
    }

    // Panel: Obtener métricas
    @GetMapping("/campañas/{id}/metricas")
    public ResponseEntity<MetricasMailingResponse> obtenerMetricas(@PathVariable Integer id) {
        MetricasMailingResponse m = service.obtenerMetricas(id);
        return ResponseEntity.ok(m);
    }
}