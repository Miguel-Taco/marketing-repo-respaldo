package pe.unmsm.crm.marketing.leads.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.leads.api.dto.LeadReportFilterDTO;
import pe.unmsm.crm.marketing.leads.application.service.LeadReportService;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/leads/reportes")
@RequiredArgsConstructor
@Slf4j
// CAMBIO AQUÍ: Usar 'origins' en lugar de 'allowedOriginPatterns' para
// versiones antiguas de Spring

public class LeadReportController {

    private final LeadReportService leadReportService;

    @GetMapping("/general/pdf")
    public ResponseEntity<byte[]> generarReporteGeneral(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            LeadReportFilterDTO filtros = LeadReportFilterDTO.builder()
                    .fechaInicio(fechaInicio)
                    .fechaFin(fechaFin)
                    .build();
            byte[] pdfBytes = leadReportService.generateGeneralReport(filtros);
            return buildPdfResponse(pdfBytes, "reporte-general-leads.pdf");
        } catch (IOException e) {
            log.error("Error al generar reporte general: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/fuentes/pdf")
    public ResponseEntity<byte[]> generarReporteFuentes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            LeadReportFilterDTO filtros = LeadReportFilterDTO.builder()
                    .fechaInicio(fechaInicio)
                    .fechaFin(fechaFin)
                    .build();
            byte[] pdfBytes = leadReportService.generateSourceReport(filtros);
            return buildPdfResponse(pdfBytes, "reporte-fuentes-leads.pdf");
        } catch (IOException e) {
            log.error("Error al generar reporte de fuentes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/conversion/pdf")
    public ResponseEntity<byte[]> generarReporteConversion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            LeadReportFilterDTO filtros = LeadReportFilterDTO.builder()
                    .fechaInicio(fechaInicio)
                    .fechaFin(fechaFin)
                    .build();
            byte[] pdfBytes = leadReportService.generateConversionReport(filtros);
            return buildPdfResponse(pdfBytes, "reporte-conversion-leads.pdf");
        } catch (IOException e) {
            log.error("Error al generar reporte de conversión: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/tendencias/pdf")
    public ResponseEntity<byte[]> generarReporteTendencias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "DIARIO") String granularidad) {
        try {
            LeadReportFilterDTO filtros = LeadReportFilterDTO.builder()
                    .fechaInicio(fechaInicio)
                    .fechaFin(fechaFin)
                    .build();
            byte[] pdfBytes = leadReportService.generateTrendsReport(filtros, granularidad);
            return buildPdfResponse(pdfBytes, "reporte-tendencias-leads.pdf");
        } catch (IOException e) {
            log.error("Error al generar reporte de tendencias: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/demografico/pdf")
    public ResponseEntity<byte[]> generarReporteDemografico(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            LeadReportFilterDTO filtros = LeadReportFilterDTO.builder()
                    .fechaInicio(fechaInicio)
                    .fechaFin(fechaFin)
                    .build();
            byte[] pdfBytes = leadReportService.generateDemographicReport(filtros);
            return buildPdfResponse(pdfBytes, "reporte-demografico-leads.pdf");
        } catch (IOException e) {
            log.error("Error al generar reporte demográfico: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdfBytes, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfBytes.length);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}