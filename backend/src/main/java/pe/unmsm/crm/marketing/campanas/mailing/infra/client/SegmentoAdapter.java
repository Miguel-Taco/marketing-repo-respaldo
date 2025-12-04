package pe.unmsm.crm.marketing.campanas.mailing.infra.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.ISegmentoPort;
import pe.unmsm.crm.marketing.shared.infra.exception.ExternalServiceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adapter para obtener información de segmentos.
 * 
 * ESTRATEGIA:
 * 1. Llama a /api/v1/internal/segmentos/{id}/miembros para obtener IDs de leads
 * 2. Con esos IDs, consulta la tabla leads para obtener los emails
 * 
 * Si la API no está disponible, usa consulta directa a BD como fallback.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SegmentoAdapter implements ISegmentoPort {

    private final RestClient restClient;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.segmentacion.url:http://localhost:8080}")
    private String segmentacionUrl;

    /**
     * Obtiene los emails de los miembros de un segmento.
     * 
     * FLUJO:
     * 1. Llama al endpoint /miembros para obtener List<Long> (IDs de leads)
     * 2. Con esos IDs, consulta la BD para obtener los emails
     */
    @Override
    public List<String> obtenerEmailsSegmento(Long idSegmento) {
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║  OBTENIENDO EMAILS DEL SEGMENTO: {}              ", idSegmento);
        log.info("╚══════════════════════════════════════════════════╝");
        
        try {
            // Paso 1: Obtener IDs de miembros via API
            List<Long> idsLeads = obtenerIdsMiembrosPorApi(idSegmento);
            
            if (idsLeads == null || idsLeads.isEmpty()) {
                log.warn("  El segmento {} no tiene miembros", idSegmento);
                return List.of();
            }
            
            log.info("  Obtenidos {} IDs de miembros", idsLeads.size());
            
            // Paso 2: Obtener emails de esos leads
            List<String> emails = obtenerEmailsPorIds(idsLeads);
            
            log.info("  ✓ {} emails obtenidos del segmento {}", emails.size(), idSegmento);
            return emails;
            
        } catch (Exception e) {
            log.warn("  API no disponible, usando consulta directa: {}", e.getMessage());
            return obtenerEmailsPorBD(idSegmento);
        }
    }

    /**
     * Llama al endpoint /api/v1/internal/segmentos/{id}/miembros
     * Retorna List<Long> con los IDs de los leads
     */
    private List<Long> obtenerIdsMiembrosPorApi(Long idSegmento) {
        String url = segmentacionUrl + "/api/v1/internal/segmentos/" + idSegmento + "/miembros";
        
        log.debug("  Llamando API: {}", url);
        
        try {
            Long[] ids = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Long[].class);
            
            if (ids == null) {
                return List.of();
            }
            
            return Arrays.asList(ids);
            
        } catch (RestClientException e) {
            log.error("  Error llamando API de miembros: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene emails de leads dado sus IDs
     */
    private List<String> obtenerEmailsPorIds(List<Long> idsLeads) {
        if (idsLeads == null || idsLeads.isEmpty()) {
            return List.of();
        }
        
        try {
            // Construir query con IN clause
            String placeholders = String.join(",", idsLeads.stream()
                    .map(id -> "?")
                    .toList());
            
            String sql = String.format(
                "SELECT email FROM leads WHERE lead_id IN (%s) AND email IS NOT NULL AND email != ''",
                placeholders
            );
            
            List<String> emails = jdbcTemplate.queryForList(
                sql, 
                String.class, 
                idsLeads.toArray()
            );
            
            log.debug("  {} emails encontrados para {} IDs", emails.size(), idsLeads.size());
            return emails;
            
        } catch (Exception e) {
            log.error("  Error obteniendo emails por IDs: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Fallback: Consulta directa a BD usando JOIN
     * Se usa cuando la API de segmentación no está disponible
     */
    private List<String> obtenerEmailsPorBD(Long idSegmento) {
        log.info("  Usando fallback: consulta directa a BD");
        
        try {
            String sql = """
                SELECT l.email 
                FROM segmento_miembro sm
                INNER JOIN leads l ON sm.id_miembro = l.lead_id
                WHERE sm.id_segmento = ?
                AND l.email IS NOT NULL
                AND l.email != ''
                """;
            
            List<String> emails = jdbcTemplate.queryForList(sql, String.class, idSegmento);
            
            log.info("  ✓ {} emails obtenidos del segmento {} vía BD", emails.size(), idSegmento);
            return emails;
            
        } catch (Exception e) {
            log.error("  Error obteniendo emails por BD: {}", e.getMessage());
            throw new ExternalServiceException("Segmentacion", 
                "No se pudieron obtener emails del segmento: " + e.getMessage());
        }
    }

    /**
     * Cuenta los miembros de un segmento
     */
    @Override
    public Integer contarMiembros(Long idSegmento) {
        try {
            // Intentar por API (endpoint resumen)
            String url = segmentacionUrl + "/api/v1/internal/segmentos/" + idSegmento + "/resumen";
            
            @SuppressWarnings("unchecked")
            var response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(java.util.Map.class);
            
            if (response != null && response.containsKey("cantidadMiembros")) {
                return (Integer) response.get("cantidadMiembros");
            }
            
        } catch (Exception e) {
            log.debug("  API de resumen no disponible, contando por BD");
        }
        
        // Fallback: contar por BD
        try {
            String sql = "SELECT COUNT(*) FROM segmento_miembro WHERE id_segmento = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idSegmento);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("  Error contando miembros: {}", e.getMessage());
            return 0;
        }
    }
}
















