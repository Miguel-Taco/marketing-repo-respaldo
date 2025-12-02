package pe.unmsm.crm.marketing.campanas.encuestas.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.campanas.encuestas.application.service.AnalyticsService;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.strategy.AnalisisResultadoDto;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/encuestas/respuestas/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // Allow frontend access
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/{idEncuesta}/tendencia")
    public ResponseEntity<List<Map<String, Object>>> getTendencia(@PathVariable Integer idEncuesta) {
        return ResponseEntity.ok(analyticsService.getTendenciaRespuestas(idEncuesta));
    }

    @GetMapping("/{id}/indicadores")
    public List<AnalisisResultadoDto> getIndicadores(
            @PathVariable Integer id,
            @RequestParam(required = false) String rango) {
        return analyticsService.getIndicadores(id, rango);
    }

    @GetMapping("/{id}/resumen")
    public pe.unmsm.crm.marketing.campanas.encuestas.api.dto.AnalyticsSummaryDto getResumen(
            @PathVariable Integer id,
            @RequestParam(required = false) String rango) {
        return analyticsService.getResumen(id, rango);
    }
}
