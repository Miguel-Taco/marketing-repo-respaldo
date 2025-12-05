package pe.unmsm.crm.marketing.campanas.telefonicas.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.CampaniaTelefonicaDTO;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.LlamadaDTO;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.MetricasCampaniaDTO;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.TelemarketingReportFilterDTO;
import pe.unmsm.crm.marketing.shared.services.HtmlTemplateService;
import pe.unmsm.crm.marketing.shared.services.PdfReportService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para generar reportes PDF de campañas telefónicas.
 * Incluye métricas de campaña y historial de llamadas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TelemarketingReportService {

    private final TelemarketingService telemarketingService;
    private final PdfReportService pdfReportService;
    private final HtmlTemplateService htmlTemplateService;

    /**
     * Genera un reporte completo de campaña telefónica en formato PDF.
     * Incluye métricas y historial de llamadas.
     *
     * @param idCampania ID de la campaña
     * @param filtros    Filtros de fecha y agente
     * @return Bytes del PDF generado
     * @throws IOException Si hay error al generar el PDF
     */
    public byte[] generateCampaignReport(Long idCampania, TelemarketingReportFilterDTO filtros) throws IOException {
        // 1. Obtener información de la campaña
        CampaniaTelefonicaDTO campania = telemarketingService.obtenerCampaniaPorId(idCampania);
        if (campania == null) {
            throw new IllegalArgumentException("Campaña no encontrada: " + idCampania);
        }

        // 2. Obtener métricas de la campaña
        Integer dias = calculateDaysBetween(filtros.getFechaInicio(), filtros.getFechaFin());
        MetricasCampaniaDTO metricas = telemarketingService.obtenerMetricasCampania(idCampania, dias);

        // 3. Obtener historial de llamadas (filtrado por agente si se especifica)
        List<LlamadaDTO> llamadas = telemarketingService.obtenerHistorialLlamadas(
                idCampania,
                filtros.getIdAgente());

        // 4. Filtrar llamadas por fecha si se especifica
        if (filtros.getFechaInicio() != null || filtros.getFechaFin() != null) {
            LocalDateTime startDateTime = getStartDateTime(filtros.getFechaInicio());
            LocalDateTime endDateTime = getEndDateTime(filtros.getFechaFin());
            llamadas = llamadas.stream()
                    .filter(l -> {
                        LocalDateTime fechaLlamada = l.getFechaHora();
                        if (fechaLlamada == null)
                            return false;
                        return !fechaLlamada.isBefore(startDateTime) && !fechaLlamada.isAfter(endDateTime);
                    })
                    .toList();
        }

        // 5. Construir datos para el template
        Map<String, Object> data = buildReportData(campania, metricas, llamadas, filtros);

        // 6. Generar HTML y PDF
        String html = htmlTemplateService.processTemplate("telemarketing-campaign-report.html", data);
        return pdfReportService.generatePdfFromHtml(html);
    }

    /**
     * Construye el mapa de datos para el template HTML.
     */
    private Map<String, Object> buildReportData(
            CampaniaTelefonicaDTO campania,
            MetricasCampaniaDTO metricas,
            List<LlamadaDTO> llamadas,
            TelemarketingReportFilterDTO filtros) {

        Map<String, Object> data = new HashMap<>();

        // Información del reporte
        data.put("reportTitle", "Reporte de Campaña Telefónica");
        data.put("reportDescription", "Métricas y historial de llamadas");
        data.put("generatedBy", "Sistema CRM");
        data.put("generatedDate", LocalDate.now());
        data.put("startDate", filtros.getFechaInicio() != null ? filtros.getFechaInicio() : "Inicio de campaña");
        data.put("endDate", filtros.getFechaFin() != null ? filtros.getFechaFin() : "Actualidad");

        // Información de la campaña
        data.put("campaignName", HtmlUtils.htmlEscape(campania.getNombre()));
        data.put("campaignDescription",
                HtmlUtils.htmlEscape(campania.getDescripcion() != null ? campania.getDescripcion() : ""));
        data.put("campaignStatus", campania.getEstado());

        // Métricas principales - calculadas desde las llamadas filtradas del reporte
        long totalLlamadasReporte = llamadas.size();
        long llamadasEfectivasReporte = llamadas.stream()
                .filter(l -> l.getResultado() != null &&
                        (l.getResultado().equalsIgnoreCase("Contactado") ||
                                l.getResultado().equalsIgnoreCase("Interesado")))
                .count();
        long llamadasNoEfectivasReporte = totalLlamadasReporte - llamadasEfectivasReporte;

        // Las pendientes son del contexto general de campaña, no de las llamadas
        long llamadasPendientesReporte = metricas != null && metricas.getLlamadasPendientes() != null
                ? metricas.getLlamadasPendientes()
                : 0L;

        data.put("totalLlamadas", totalLlamadasReporte);
        data.put("llamadasEfectivas", llamadasEfectivasReporte);
        data.put("llamadasNoEfectivas", llamadasNoEfectivasReporte);
        data.put("llamadasPendientes", llamadasPendientesReporte);
        data.put("tasaEfectividad",
                calculatePercentage(llamadasEfectivasReporte, totalLlamadasReporte));
        data.put("promedioLlamadas",
                String.format("%.1f", metricas != null && metricas.getPromedioLlamadasDiarias() != null
                        ? metricas.getPromedioLlamadasDiarias()
                        : 0.0));

        // Historial de llamadas
        data.put("callHistoryRows", buildCallHistoryRows(llamadas));
        data.put("totalCallsInReport", llamadas.size());

        return data;
    }

    /**
     * Construye las filas HTML para la tabla de historial de llamadas.
     */
    private String buildCallHistoryRows(List<LlamadaDTO> llamadas) {
        if (llamadas == null || llamadas.isEmpty()) {
            return "<tr><td colspan='6' style='text-align: center;'>No hay llamadas registradas</td></tr>";
        }

        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (LlamadaDTO llamada : llamadas) {
            sb.append("<tr>");

            // Fecha
            sb.append("<td>")
                    .append(llamada.getFechaHora() != null ? llamada.getFechaHora().format(formatter) : "N/A")
                    .append("</td>");

            // Teléfono
            sb.append("<td>")
                    .append(HtmlUtils
                            .htmlEscape(llamada.getTelefonoContacto() != null ? llamada.getTelefonoContacto() : "N/A"))
                    .append("</td>");

            // Resultado
            sb.append("<td>")
                    .append(HtmlUtils.htmlEscape(llamada.getResultado() != null ? llamada.getResultado() : "N/A"))
                    .append("</td>");

            // Duración (en minutos)
            String duracion = "N/A";
            if (llamada.getDuracionSegundos() != null) {
                long minutos = llamada.getDuracionSegundos() / 60;
                long segundos = llamada.getDuracionSegundos() % 60;
                duracion = String.format("%d:%02d", minutos, segundos);
            }
            sb.append("<td>").append(duracion).append("</td>");

            // Agente
            sb.append("<td>")
                    .append(llamada.getNombreAgente() != null ? HtmlUtils.htmlEscape(llamada.getNombreAgente()) : "N/A")
                    .append("</td>");

            // Observaciones
            sb.append("<td>")
                    .append(llamada.getNotas() != null ? HtmlUtils.htmlEscape(llamada.getNotas()) : "")
                    .append("</td>");

            sb.append("</tr>");
        }

        return sb.toString();
    }

    /**
     * Calcula el porcentaje con formato.
     */
    private String calculatePercentage(Long part, Long total) {
        if (total == null || total == 0 || part == null) {
            return "0.0%";
        }
        return String.format("%.1f%%", (double) part / total * 100);
    }

    /**
     * Calcula los días entre dos fechas.
     */
    private Integer calculateDaysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 30; // Por defecto 30 días
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Obtiene el LocalDateTime de inicio.
     */
    private LocalDateTime getStartDateTime(LocalDate date) {
        return date != null ? date.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
    }

    /**
     * Obtiene el LocalDateTime de fin.
     */
    private LocalDateTime getEndDateTime(LocalDate date) {
        return date != null ? date.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);
    }
}
