package pe.unmsm.crm.marketing.leads.api.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import pe.unmsm.crm.marketing.leads.api.dto.LeadIntegrationDTO;
import pe.unmsm.crm.marketing.leads.application.service.LeadIntegrationService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/internal/leads")
@RequiredArgsConstructor
public class LeadIntegrationController {

    private final LeadIntegrationService integrationService;

    @GetMapping("/segmentation")
    public ResponseEntity<List<LeadIntegrationDTO>> obtenerLeadsParaSegmentacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer edadMin,
            @RequestParam(required = false) Integer edadMax,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) String distritoId,
            @RequestParam(required = false) String provinciaId,
            @RequestParam(required = false) String departamentoId,
            @RequestParam(required = false) String nivelEducativo,
            @RequestParam(required = false) String estadoCivil) {

        List<LeadIntegrationDTO> leads = integrationService.obtenerLeadsParaSegmentacion(
                fechaDesde, fechaHasta,
                edadMin, edadMax, genero,
                distritoId, provinciaId, departamentoId,
                nivelEducativo, estadoCivil);
        return ResponseEntity.ok(leads);
    }

    /**
     * Endpoint para obtener TODOS los leads (para filtrado en memoria por
     * Segmentación)
     */
    @GetMapping("/all")
    public ResponseEntity<List<LeadIntegrationDTO>> obtenerTodosLosLeads() {
        // Usar rango de fechas amplio para obtener todos los leads
        LocalDate fechaDesde = LocalDate.of(2000, 1, 1);
        LocalDate fechaHasta = LocalDate.of(2100, 12, 31);

        List<LeadIntegrationDTO> leads = integrationService.obtenerLeadsParaSegmentacion(
                fechaDesde, fechaHasta,
                null, null, null, null, null, null, null, null);
        return ResponseEntity.ok(leads);
    }
}