package pe.unmsm.crm.marketing.leads.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.unmsm.crm.marketing.leads.api.dto.LeadReportFilterDTO;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoLead;
import pe.unmsm.crm.marketing.leads.domain.enums.TipoFuente;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.leads.domain.repository.LeadRepository;
import pe.unmsm.crm.marketing.shared.services.HtmlTemplateService;
import pe.unmsm.crm.marketing.shared.services.PdfReportService;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadReportService {

    private final LeadRepository leadRepository;
    private final PdfReportService pdfReportService;
    private final HtmlTemplateService htmlTemplateService;

    public byte[] generateGeneralReport(LeadReportFilterDTO filtros) throws IOException {
        LocalDateTime startDateTime = getStartDateTime(filtros.getFechaInicio());
        LocalDateTime endDateTime = getEndDateTime(filtros.getFechaFin());

        // 1. Obtener métricas generales (Optimizado)
        Map<EstadoLead, Long> countsByEstado = getCountsByEstado(startDateTime, endDateTime);

        long totalLeads = leadRepository.countByFechaCreacionBetween(startDateTime, endDateTime);
        long leadsNuevos = countsByEstado.getOrDefault(EstadoLead.NUEVO, 0L);
        long leadsCalificados = countsByEstado.getOrDefault(EstadoLead.CALIFICADO, 0L);
        long leadsDescartados = countsByEstado.getOrDefault(EstadoLead.DESCARTADO, 0L);

        // 2. Obtener lista de leads reciente
        List<Lead> leads = leadRepository.findByFechaCreacionBetweenOrderByFechaCreacionAsc(startDateTime, endDateTime);
        if (leads.size() > 50) {
            leads = leads.subList(leads.size() - 50, leads.size());
        }

        // 3. Construir datos
        Map<String, Object> data = buildBaseData("Reporte General de Leads",
                "Resumen de actividad y métricas de captación de leads", startDateTime, endDateTime);

        data.put("totalLeads", totalLeads);
        data.put("leadsNuevos", leadsNuevos);
        data.put("leadsCalificados", leadsCalificados);
        data.put("leadsDescartados", leadsDescartados);
        data.put("tasaConversion", calculatePercentage(leadsCalificados, totalLeads));
        data.put("leadsTableRows", buildLeadsTableRows(leads));

        String html = htmlTemplateService.processTemplate("lead-general-report.html", data);
        return pdfReportService.generatePdfFromHtml(html);
    }

    public byte[] generateSourceReport(LeadReportFilterDTO filtros) throws IOException {
        LocalDateTime startDateTime = getStartDateTime(filtros.getFechaInicio());
        LocalDateTime endDateTime = getEndDateTime(filtros.getFechaFin());

        // 1. Métricas por fuente
        Map<TipoFuente, Long> countsByFuente = getCountsByFuente(startDateTime, endDateTime);

        long totalWeb = countsByFuente.getOrDefault(TipoFuente.WEB, 0L);
        long totalImport = countsByFuente.getOrDefault(TipoFuente.IMPORTACION, 0L);
        long total = totalWeb + totalImport;

        // 2. Construir datos
        Map<String, Object> data = buildBaseData("Reporte de Fuentes de Captación",
                "Comparativa de rendimiento por canal de adquisición", startDateTime, endDateTime);

        data.put("totalWeb", totalWeb);
        data.put("totalImport", totalImport);
        data.put("porcentajeWeb", calculatePercentage(totalWeb, total));
        data.put("porcentajeImport", calculatePercentage(totalImport, total));

        String html = htmlTemplateService.processTemplate("lead-source-report.html", data);
        return pdfReportService.generatePdfFromHtml(html);
    }

    public byte[] generateConversionReport(LeadReportFilterDTO filtros) throws IOException {
        LocalDateTime startDateTime = getStartDateTime(filtros.getFechaInicio());
        LocalDateTime endDateTime = getEndDateTime(filtros.getFechaFin());

        Map<EstadoLead, Long> countsByEstado = getCountsByEstado(startDateTime, endDateTime);
        long total = countsByEstado.values().stream().mapToLong(Long::longValue).sum();
        long calificados = countsByEstado.getOrDefault(EstadoLead.CALIFICADO, 0L);
        long descartados = countsByEstado.getOrDefault(EstadoLead.DESCARTADO, 0L);

        Map<String, Object> data = buildBaseData("Reporte de Conversión",
                "Análisis del embudo de conversión de leads", startDateTime, endDateTime);

        data.put("totalLeads", total);
        data.put("calificados", calificados);
        data.put("descartados", descartados);
        data.put("tasaConversion", calculatePercentage(calificados, total));
        data.put("tasaDescarte", calculatePercentage(descartados, total));

        String html = htmlTemplateService.processTemplate("lead-conversion-report.html", data);
        return pdfReportService.generatePdfFromHtml(html);
    }

    public byte[] generateTrendsReport(LeadReportFilterDTO filtros, String granularidad) throws IOException {
        LocalDateTime startDateTime = getStartDateTime(filtros.getFechaInicio());
        LocalDateTime endDateTime = getEndDateTime(filtros.getFechaFin());

        List<Lead> leads = leadRepository.findByFechaCreacionBetweenOrderByFechaCreacionAsc(startDateTime, endDateTime);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Long> trendData = leads.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getFechaCreacion().format(formatter),
                        Collectors.counting()));

        Map<String, Object> data = buildBaseData("Reporte de Tendencias",
                "Evolución temporal de la captación de leads", startDateTime, endDateTime);

        data.put("trendRows", buildTrendRows(trendData));
        data.put("totalPeriodo", leads.size());

        String html = htmlTemplateService.processTemplate("lead-trends-report.html", data);
        return pdfReportService.generatePdfFromHtml(html);
    }

    public byte[] generateDemographicReport(LeadReportFilterDTO filtros) throws IOException {
        LocalDateTime startDateTime = getStartDateTime(filtros.getFechaInicio());
        LocalDateTime endDateTime = getEndDateTime(filtros.getFechaFin());

        List<Lead> leads = leadRepository.findByFechaCreacionBetweenOrderByFechaCreacionAsc(startDateTime, endDateTime);

        Map<String, Long> byGenero = leads.stream()
                .filter(l -> l.getDemograficos() != null && l.getDemograficos().getGenero() != null)
                .collect(Collectors.groupingBy(l -> l.getDemograficos().getGenero(), Collectors.counting()));

        Map<String, Long> byDistrito = leads.stream()
                .filter(l -> l.getDemograficos() != null && l.getDemograficos().getDistrito() != null)
                .collect(Collectors.groupingBy(l -> l.getDemograficos().getDistrito(), Collectors.counting()));

        Map<String, Object> data = buildBaseData("Reporte Demográfico",
                "Análisis del perfil demográfico de los leads", startDateTime, endDateTime);

        data.put("genderRows", buildMapRows(byGenero, "Género"));
        data.put("districtRows", buildMapRows(byDistrito, "Distrito"));
        data.put("totalAnalizados", leads.size());

        String html = htmlTemplateService.processTemplate("lead-demographic-report.html", data);
        return pdfReportService.generatePdfFromHtml(html);
    }

    // Helpers
    private Map<String, Object> buildBaseData(String title, String description, LocalDateTime start,
            LocalDateTime end) {
        Map<String, Object> data = new HashMap<>();
        data.put("reportTitle", title);
        data.put("reportDescription", description);
        data.put("generatedBy", "Sistema CRM");
        data.put("generatedDate", LocalDate.now());
        data.put("startDate", start.toLocalDate());
        data.put("endDate", end.toLocalDate());
        return data;
    }

    private String calculatePercentage(long part, long total) {
        return total > 0 ? String.format("%.1f%%", (double) part / total * 100) : "0.0%";
    }

    private Map<EstadoLead, Long> getCountsByEstado(LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = leadRepository.countByEstadoBetween(start, end);
        Map<EstadoLead, Long> map = new HashMap<>();
        for (Object[] row : results) {
            map.put((EstadoLead) row[0], (Long) row[1]);
        }
        return map;
    }

    private Map<TipoFuente, Long> getCountsByFuente(LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = leadRepository.countByFuenteTipoBetween(start, end);
        Map<TipoFuente, Long> map = new HashMap<>();
        for (Object[] row : results) {
            map.put((TipoFuente) row[0], (Long) row[1]);
        }
        return map;
    }

    private LocalDateTime getStartDateTime(LocalDate date) {
        return date != null ? date.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
    }

    private LocalDateTime getEndDateTime(LocalDate date) {
        return date != null ? date.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);
    }

    private String buildLeadsTableRows(List<Lead> leads) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Lead lead : leads) {
            sb.append("<tr>");
            sb.append("<td>").append(lead.getId()).append("</td>");
            sb.append("<td>").append(HtmlUtils.htmlEscape(lead.getNombre() != null ? lead.getNombre() : "N/A"))
                    .append("</td>");
            sb.append("<td>").append(lead.getEstado()).append("</td>");
            sb.append("<td>").append(lead.getFuenteTipo()).append("</td>");
            sb.append("<td>").append(lead.getFechaCreacion().format(formatter)).append("</td>");
            sb.append("</tr>");
        }
        return sb.toString();
    }

    private String buildTrendRows(Map<String, Long> trendData) {
        StringBuilder sb = new StringBuilder();
        trendData.forEach((date, count) -> {
            sb.append("<tr>");
            sb.append("<td>").append(date).append("</td>");
            sb.append("<td>").append(count).append("</td>");
            sb.append("</tr>");
        });
        return sb.toString();
    }

    private String buildMapRows(Map<String, Long> map, String label) {
        StringBuilder sb = new StringBuilder();
        map.forEach((key, count) -> {
            sb.append("<tr>");
            sb.append("<td>").append(HtmlUtils.htmlEscape(key != null ? key : "N/A")).append("</td>");
            sb.append("<td>").append(count).append("</td>");
            sb.append("</tr>");
        });
        return sb.toString();
    }
}
