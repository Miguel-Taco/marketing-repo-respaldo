package pe.unmsm.crm.marketing.campanas.telefonicas.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.TelemarketingReportFilterDTO;
import pe.unmsm.crm.marketing.campanas.telefonicas.application.TelemarketingReportService;
import pe.unmsm.crm.marketing.security.service.UserAuthorizationService;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Controlador REST para generación de reportes de campañas telefónicas.
 */
@RestController
@RequestMapping("/api/v1/campanias-telefonicas")
@RequiredArgsConstructor
@Slf4j
public class TelemarketingReportController {

    private final TelemarketingReportService reportService;
    private final UserAuthorizationService userAuthorizationService;

    /**
     * Genera un reporte PDF de una campaña telefónica.
     * Incluye métricas y historial de llamadas.
     * 
     * @param id          ID de la campaña
     * @param fechaInicio Fecha de inicio del periodo (opcional)
     * @param fechaFin    Fecha de fin del periodo (opcional)
     * @param idAgente    ID del agente para filtrar (opcional, solo admins)
     * @return PDF del reporte
     */
    @GetMapping("/{id}/reporte/pdf")
    public ResponseEntity<byte[]> generarReporteCampania(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long idAgente) {

        try {
            // Verificar acceso a la campaña
            userAuthorizationService.ensureCampaniaTelefonicaAccess(id);

            // Resolver el agente según el rol del usuario
            Long resolvedAgente = resolveAgentForReport(idAgente);

            // Construir filtros
            TelemarketingReportFilterDTO filtros = TelemarketingReportFilterDTO.builder()
                    .fechaInicio(fechaInicio)
                    .fechaFin(fechaFin)
                    .idAgente(resolvedAgente)
                    .build();

            // Generar reporte
            byte[] pdfBytes = reportService.generateCampaignReport(id, filtros);

            // Construir respuesta
            return buildPdfResponse(pdfBytes, "reporte-campania-" + id + ".pdf");

        } catch (IllegalArgumentException e) {
            log.error("Error en parámetros del reporte: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IOException e) {
            log.error("Error al generar reporte de campaña {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error inesperado al generar reporte: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Resuelve el agente para el reporte según el rol del usuario.
     * - Si es ADMIN y no se especifica agente, devuelve null (todos los agentes).
     * - Si es ADMIN y se especifica agente, valida y devuelve ese agente.
     * - Si es AGENTE, devuelve el ID del agente actual (ignora el parámetro).
     */
    private Long resolveAgentForReport(Long idAgente) {
        if (userAuthorizationService.isAdmin()) {
            // Admin puede ver todos o filtrar por agente específico
            if (idAgente != null) {
                // Validar que el agente existe
                userAuthorizationService.ensureAgentAccess(idAgente);
            }
            return idAgente; // null = todos, o el agente específico
        } else {
            // Agente solo puede ver sus propias llamadas
            return userAuthorizationService.requireCurrentAgentId().longValue();
        }
    }

    /**
     * Construye la respuesta HTTP con el PDF.
     */
    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdfBytes, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfBytes.length);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
