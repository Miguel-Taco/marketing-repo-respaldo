package pe.unmsm.crm.marketing.campanas.gestor.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.unmsm.crm.marketing.campanas.gestor.api.dto.GestorReportFilterDTO;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.CanalEjecucion;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.CampanaRepositoryPort;
import pe.unmsm.crm.marketing.campanas.gestor.domain.state.EstadoCampana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.state.converter.EstadoCampanaConverter;
import pe.unmsm.crm.marketing.campanas.gestor.infra.persistence.repository.JpaPlantillaRepository;
import pe.unmsm.crm.marketing.shared.services.HtmlTemplateService;
import pe.unmsm.crm.marketing.shared.services.PdfReportService;
import pe.unmsm.crm.marketing.segmentacion.infra.persistence.JpaSegmentoRepository;
import pe.unmsm.crm.marketing.segmentacion.infra.persistence.JpaSegmentoEntity;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.PlantillaCampana;

import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestorReportService {

    private final CampanaRepositoryPort campanaRepository;
    private final PdfReportService pdfReportService;
    private final HtmlTemplateService htmlTemplateService;
    private final JpaSegmentoRepository segmentoRepository;
    private final JpaPlantillaRepository plantillaRepository;

    public byte[] generateGeneralReport(GestorReportFilterDTO filtros) throws IOException {
        LocalDateTime startDateTime = getStartDateTime(filtros.getFechaInicio());
        LocalDateTime endDateTime = getEndDateTime(filtros.getFechaFin());

        List<Object[]> estadoCounts = campanaRepository.countByEstadoBetween(startDateTime, endDateTime);
        Map<String, Long> countsByEstado = new HashMap<>();
        EstadoCampanaConverter converter = new EstadoCampanaConverter();

        for (Object[] row : estadoCounts) {
            EstadoCampana est = (EstadoCampana) row[0];
            String displayName = est.getClass().getSimpleName().replace("Estado", "");
            countsByEstado.put(displayName, (Long) row[1]);
        }

        List<Object[]> canalCounts = campanaRepository.countByCanalBetween(startDateTime, endDateTime);
        Map<String, Long> countsByCanal = new HashMap<>();
        for (Object[] row : canalCounts) {
            CanalEjecucion canal = (CanalEjecucion) row[0];
            countsByCanal.put(canal.name(), (Long) row[1]);
        }

        List<Campana> camps = campanaRepository.findByFechaCreacionBetweenOrderByFechaCreacionAsc(startDateTime,
                endDateTime);
        long totalCampanas = camps.size();

        Map<String, Object> data = buildBaseData("Reporte General de Gestión de Campañas",
                "Visión estratégica del ciclo de vida y ejecución de campañas", startDateTime, endDateTime);

        data.put("totalCampanas", totalCampanas);
        data.put("countsByEstado", countsByEstado);
        data.put("countsByCanal", countsByCanal);

        long vigentes = countsByEstado.getOrDefault("Vigente", 0L);
        long pausadas = countsByEstado.getOrDefault("Pausada", 0L);
        long finalizadas = countsByEstado.getOrDefault("Finalizada", 0L);

        data.put("vigentes", vigentes);
        data.put("pausadas", pausadas);
        data.put("finalizadas", finalizadas);

        data.put("estadoRows", buildMapRows(countsByEstado));
        data.put("canalRows", buildMapRows(countsByCanal));

        List<Campana> recentCamps = camps;
        if (recentCamps.size() > 50) {
            recentCamps = recentCamps.subList(recentCamps.size() - 50, recentCamps.size());
        }
        data.put("campanaRows", buildCampanaRows(recentCamps));

        String html = htmlTemplateService.processTemplate("gestor-general-report.html", data);
        return pdfReportService.generatePdfFromHtml(html);
    }

    public byte[] generateEfficiencyReport(GestorReportFilterDTO filtros) throws IOException {
        LocalDateTime startDateTime = getStartDateTime(filtros.getFechaInicio());
        LocalDateTime endDateTime = getEndDateTime(filtros.getFechaFin());

        List<Campana> camps = campanaRepository.findByFechaCreacionBetweenOrderByFechaCreacionAsc(startDateTime,
                endDateTime);
        long total = camps.size();

        long programadas = camps.stream().filter(c -> c.getEstado().getClass().getSimpleName().contains("Programada"))
                .count();
        long canceladas = camps.stream().filter(c -> c.getEstado().getClass().getSimpleName().contains("Cancelada"))
                .count();

        Map<String, Object> data = buildBaseData("Reporte de Eficiencia",
                "Análisis de programación y cumplimiento", startDateTime, endDateTime);

        data.put("totalCampanas", total);
        data.put("tasaCancelacion", calculatePercentage(canceladas, total));
        data.put("programadas", programadas);
        data.put("canceladas", canceladas);

        String html = htmlTemplateService.processTemplate("gestor-efficiency-report.html", data);
        return pdfReportService.generatePdfFromHtml(html);
    }

    public byte[] generateResourceUsageReport(GestorReportFilterDTO filtros) throws IOException {
        LocalDateTime startDateTime = getStartDateTime(filtros.getFechaInicio());
        LocalDateTime endDateTime = getEndDateTime(filtros.getFechaFin());

        // Segmentos
        List<Object[]> segmentoCounts = campanaRepository.countBySegmentoBetween(startDateTime, endDateTime);
        Map<String, Long> countsBySegmento = new HashMap<>();
        for (Object[] row : segmentoCounts) {
            Long idSegmento = (Long) row[0];
            Long count = (Long) row[1];
            String name = "ID: " + idSegmento;

            // Try to fetch name
            if (idSegmento != null) {
                Optional<JpaSegmentoEntity> seg = segmentoRepository.findById(idSegmento);
                if (seg.isPresent()) {
                    name = seg.get().getNombre();
                }
            }
            countsBySegmento.put(name, count);
        }

        // Plantillas
        List<Object[]> plantillaCounts = campanaRepository.countByPlantillaBetween(startDateTime, endDateTime);
        Map<String, Long> countsByPlantilla = new HashMap<>();
        for (Object[] row : plantillaCounts) {
            Integer idPlantilla = (Integer) row[0];
            Long count = (Long) row[1];
            String name = "ID: " + idPlantilla;

            if (idPlantilla != null) {
                Optional<PlantillaCampana> plan = plantillaRepository.findById(idPlantilla);
                if (plan.isPresent()) {
                    name = plan.get().getNombre();
                }
            }
            countsByPlantilla.put(name, count);
        }

        Map<String, Object> data = buildBaseData("Reporte de Uso de Recursos",
                "Métricas de utilización de segmentos y plantillas", startDateTime, endDateTime);

        data.put("segmentoRows", buildMapRows(countsBySegmento));
        data.put("plantillaRows", buildMapRows(countsByPlantilla));

        // Totals
        data.put("totalSegmentosUsados", countsBySegmento.size());
        data.put("totalPlantillasUsadas", countsByPlantilla.size());

        String html = htmlTemplateService.processTemplate("gestor-resource-report.html", data);
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

    private LocalDateTime getStartDateTime(LocalDate date) {
        return date != null ? date.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
    }

    private LocalDateTime getEndDateTime(LocalDate date) {
        return date != null ? date.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);
    }

    private String calculatePercentage(long part, long total) {
        return total > 0 ? String.format("%.1f%%", (double) part / total * 100) : "0.0%";
    }

    private String buildMapRows(Map<String, Long> map) {
        StringBuilder sb = new StringBuilder();
        map.forEach((key, count) -> {
            sb.append("<tr>");
            sb.append("<td>").append(HtmlUtils.htmlEscape(key != null ? key : "N/A")).append("</td>");
            sb.append("<td>").append(count).append("</td>");
            sb.append("</tr>");
        });
        return sb.toString();
    }

    private String buildCampanaRows(List<Campana> camps) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Campana c : camps) {
            String estadoStr = c.getEstado().getClass().getSimpleName().replace("Estado", "");

            sb.append("<tr>");
            sb.append("<td>").append(c.getIdCampana()).append("</td>");
            sb.append("<td>").append(HtmlUtils.htmlEscape(c.getNombre())).append("</td>");
            sb.append("<td>").append(estadoStr).append("</td>");
            sb.append("<td>").append(c.getCanalEjecucion()).append("</td>");
            sb.append("<td>")
                    .append(c.getFechaCreacion() != null ? c.getFechaCreacion().format(formatter) : "-")
                    .append("</td>");
            sb.append("<td>")
                    .append(c.getFechaProgramadaInicio() != null ? c.getFechaProgramadaInicio().format(formatter) : "-")
                    .append("</td>");
            sb.append("</tr>");
        }
        return sb.toString();
    }
}
