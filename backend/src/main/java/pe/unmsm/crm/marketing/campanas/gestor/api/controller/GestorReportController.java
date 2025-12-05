package pe.unmsm.crm.marketing.campanas.gestor.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.GestorReportFilterDTO;
import pe.unmsm.crm.marketing.campanas.gestor.application.service.GestorReportService;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/marketing/campanas/gestor/reportes")
@RequiredArgsConstructor
public class GestorReportController {

    private final GestorReportService reportService;

    @GetMapping("/general")
    public ResponseEntity<byte[]> getGeneralReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String canal) throws IOException {

        GestorReportFilterDTO filtros = new GestorReportFilterDTO(fechaInicio, fechaFin, estado, canal);
        byte[] pdfBytes = reportService.generateGeneralReport(filtros);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-campanas-general.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/eficiencia")
    public ResponseEntity<byte[]> getEfficiencyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin)
            throws IOException {

        GestorReportFilterDTO filtros = new GestorReportFilterDTO(fechaInicio, fechaFin, null, null);
        byte[] pdfBytes = reportService.generateEfficiencyReport(filtros);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-campanas-eficiencia.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/uso")
    public ResponseEntity<byte[]> getResourceUsageReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin)
            throws IOException {

        GestorReportFilterDTO filtros = new GestorReportFilterDTO(fechaInicio, fechaFin, null, null);
        byte[] pdfBytes = reportService.generateResourceUsageReport(filtros);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-campanas-uso-recursos.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
