package pe.unmsm.crm.marketing.shared.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.shared.services.HtmlTemplateService;
import pe.unmsm.crm.marketing.shared.services.PdfReportService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de ejemplo para endpoints de reportes PDF.
 * Muestra cómo usar los servicios de generación de PDF.
 */
@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReportController {

    private final PdfReportService pdfReportService;
    private final HtmlTemplateService htmlTemplateService;

    /**
     * Endpoint de ejemplo: genera un reporte genérico en PDF
     * GET /api/v1/reportes/ejemplo/pdf
     */
    @GetMapping("/ejemplo/pdf")
    public ResponseEntity<byte[]> generarReporteEjemplo(
            @RequestParam(required = false, defaultValue = "Reporte de Ejemplo") String titulo,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        try {
            // 1. Construir datos del reporte
            Map<String, Object> reportData = buildExampleReportData(titulo, startDate, endDate);

            // 2. Procesar plantilla HTML
            String html = htmlTemplateService.processTemplate("report-template.html", reportData);

            // 3. Generar PDF
            byte[] pdfBytes = pdfReportService.generatePdfFromHtml(html);

            // 4. Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "reporte-ejemplo.pdf");
            headers.setContentLength(pdfBytes.length);

            log.info("Reporte de ejemplo generado exitosamente");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error al generar reporte PDF: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint de ejemplo: genera un reporte de campaña en PDF
     * GET /api/v1/reportes/campanas/{idCampana}/pdf
     */
    @GetMapping("/campanas/{idCampana}/pdf")
    public ResponseEntity<byte[]> generarReporteCampana(
            @PathVariable Long idCampana,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        try {
            // 1. Obtener datos del reporte (aquí irían consultas reales a la BD)
            Map<String, Object> reportData = buildCampanaReportData(idCampana, startDate, endDate);

            // 2. Procesar plantilla HTML
            String html = htmlTemplateService.processTemplate("report-template.html", reportData);

            // 3. Generar PDF
            byte[] pdfBytes = pdfReportService.generatePdfFromHtml(html);

            // 4. Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "reporte-campana-" + idCampana + ".pdf");
            headers.setContentLength(pdfBytes.length);

            log.info("Reporte de campaña {} generado exitosamente", idCampana);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error al generar reporte PDF para campaña {}: {}", idCampana, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Método helper para construir datos de ejemplo genérico
     */
    private Map<String, Object> buildExampleReportData(String titulo, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> data = new HashMap<>();

        // Datos básicos del reporte
        data.put("reportTitle", titulo);
        data.put("reportDescription", "Este es un reporte de ejemplo generado por el sistema CRM de Marketing");
        data.put("generatedBy", "Sistema CRM");
        data.put("generatedDate", LocalDate.now());
        data.put("startDate", startDate != null ? startDate : LocalDate.now().minusMonths(1));
        data.put("endDate", endDate != null ? endDate : LocalDate.now());

        // Criterios de búsqueda (ejemplo)
        data.put("criteriaItems", buildCriteriaHtml());

        // Métricas principales (ejemplo)
        data.put("metricsCards", buildMetricsHtml());

        // Tabla headers
        data.put("tableHeaders",
                "<th>ID</th>" +
                        "<th>Nombre</th>" +
                        "<th>Estado</th>" +
                        "<th class=\"text-center\">Fecha</th>" +
                        "<th class=\"text-right\">Valor</th>");

        // Tabla rows (ejemplo)
        data.put("tableRows", buildTableRowsHtml());

        // Footer
        data.put("platformName", "CRM Marketing UNMSM");
        data.put("pageNumber", "1");
        data.put("totalPages", "1");

        // Contenido personalizado (vacío en este ejemplo)
        data.put("customContent", "");

        return data;
    }

    /**
     * Método helper para construir datos de reporte de campaña
     */
    private Map<String, Object> buildCampanaReportData(Long idCampana, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> data = new HashMap<>();

        // Datos básicos
        data.put("reportTitle", "Informe de Campaña #" + idCampana);
        data.put("reportDescription", "Reporte detallado de métricas y resultados de la campaña telefónica");
        data.put("generatedBy", "Sistema CRM");
        data.put("generatedDate", LocalDate.now());
        data.put("startDate", startDate != null ? startDate : LocalDate.now().minusMonths(1));
        data.put("endDate", endDate != null ? endDate : LocalDate.now());

        // Criterios específicos de campaña
        data.put("criteriaItems",
                "<div class=\"criteria-row\">" +
                        "<div class=\"criteria-item\">" +
                        "<label>Estatus</label>" +
                        "<span class=\"criteria-value success\">Activa</span>" +
                        "</div>" +
                        "<div class=\"criteria-item\">" +
                        "<label>Tipo</label>" +
                        "<span class=\"criteria-value\">Telefónica</span>" +
                        "</div>" +
                        "<div class=\"criteria-item\">" +
                        "<label>Agentes</label>" +
                        "<span class=\"criteria-value\">5 Asignados</span>" +
                        "</div>" +
                        "</div>");

        // Métricas de campaña
        data.put("metricsCards",
                "<div class=\"metric-card\">" +
                        "<span class=\"metric-label\">Total Llamadas</span>" +
                        "<span class=\"metric-value\">1,234</span>" +
                        "</div>" +
                        "<div class=\"metric-card primary\">" +
                        "<span class=\"metric-label\">Llamadas Efectivas</span>" +
                        "<span class=\"metric-value\">567</span>" +
                        "</div>" +
                        "<div class=\"metric-card\">" +
                        "<span class=\"metric-label\">Tasa de Éxito</span>" +
                        "<span class=\"metric-value\">45.9%</span>" +
                        "</div>" +
                        "<div class=\"metric-card\">" +
                        "<span class=\"metric-label\">Duración Promedio</span>" +
                        "<span class=\"metric-value\">3:45 min</span>" +
                        "</div>");

        // Tabla de llamadas
        data.put("tableHeaders",
                "<th>ID Lead</th>" +
                        "<th>Nombre</th>" +
                        "<th>Resultado</th>" +
                        "<th class=\"text-center\">Fecha Llamada</th>" +
                        "<th class=\"text-right\">Duración</th>");

        data.put("tableRows",
                "<tr>" +
                        "<td class=\"text-muted\">LEAD-001</td>" +
                        "<td class=\"font-semibold\">Juan Pérez</td>" +
                        "<td><span class=\"status-badge status-active\">Contactado</span></td>" +
                        "<td class=\"text-center\">28/11/2025</td>" +
                        "<td class=\"text-right\">4:32</td>" +
                        "</tr>" +
                        "<tr>" +
                        "<td class=\"text-muted\">LEAD-002</td>" +
                        "<td class=\"font-semibold\">María García</td>" +
                        "<td><span class=\"status-badge status-warning\">No Contesta</span></td>" +
                        "<td class=\"text-center\">28/11/2025</td>" +
                        "<td class=\"text-right\">0:00</td>" +
                        "</tr>" +
                        "<tr>" +
                        "<td class=\"text-muted\">LEAD-003</td>" +
                        "<td class=\"font-semibold\">Carlos López</td>" +
                        "<td><span class=\"status-badge status-active\">Interesado</span></td>" +
                        "<td class=\"text-center\">29/11/2025</td>" +
                        "<td class=\"text-right\">5:18</td>" +
                        "</tr>");

        // Footer
        data.put("platformName", "CRM Marketing UNMSM");
        data.put("pageNumber", "1");
        data.put("totalPages", "1");
        data.put("customContent", "");

        return data;
    }

    /**
     * Helper para generar HTML de criterios
     */
    private String buildCriteriaHtml() {
        return "<div class=\"criteria-row\">" +
                "<div class=\"criteria-item\">" +
                "<label>Tipo</label>" +
                "<span class=\"criteria-value\">General</span>" +
                "</div>" +
                "<div class=\"criteria-item\">" +
                "<label>Estado</label>" +
                "<span class=\"criteria-value success\">Activo</span>" +
                "</div>" +
                "<div class=\"criteria-item\">" +
                "<label>Filtro</label>" +
                "<span class=\"criteria-value\">Todos</span>" +
                "</div>" +
                "</div>";
    }

    /**
     * Helper para generar HTML de métricas
     */
    private String buildMetricsHtml() {
        return "<div class=\"metric-card\">" +
                "<span class=\"metric-label\">Total Registros</span>" +
                "<span class=\"metric-value\">450</span>" +
                "</div>" +
                "<div class=\"metric-card primary\">" +
                "<span class=\"metric-label\">Procesados</span>" +
                "<span class=\"metric-value\">387</span>" +
                "</div>" +
                "<div class=\"metric-card\">" +
                "<span class=\"metric-label\">Pendientes</span>" +
                "<span class=\"metric-value\">63</span>" +
                "</div>" +
                "<div class=\"metric-card\">" +
                "<span class=\"metric-label\">Tasa Éxito</span>" +
                "<span class=\"metric-value\">86%</span>" +
                "</div>";
    }

    /**
     * Helper para generar HTML de filas de tabla
     */
    private String buildTableRowsHtml() {
        return "<tr>" +
                "<td class=\"text-muted\">001</td>" +
                "<td class=\"font-semibold\">Elemento Uno</td>" +
                "<td><span class=\"status-badge status-active\">Activo</span></td>" +
                "<td class=\"text-center\">30/11/2025</td>" +
                "<td class=\"text-right\">$1,234.00</td>" +
                "</tr>" +
                "<tr>" +
                "<td class=\"text-muted\">002</td>" +
                "<td class=\"font-semibold\">Elemento Dos</td>" +
                "<td><span class=\"status-badge status-inactive\">Inactivo</span></td>" +
                "<td class=\"text-center\">29/11/2025</td>" +
                "<td class=\"text-right\">$567.00</td>" +
                "</tr>" +
                "<tr>" +
                "<td class=\"text-muted\">003</td>" +
                "<td class=\"font-semibold\">Elemento Tres</td>" +
                "<td><span class=\"status-badge status-warning\">Pendiente</span></td>" +
                "<td class=\"text-center\">28/11/2025</td>" +
                "<td class=\"text-right\">$890.00</td>" +
                "</tr>";
    }
}
