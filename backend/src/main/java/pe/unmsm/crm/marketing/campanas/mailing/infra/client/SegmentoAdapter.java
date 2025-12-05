package pe.unmsm.crm.marketing.campanas.mailing.infra.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.ISegmentoPort;
import pe.unmsm.crm.marketing.shared.infra.exception.ExternalServiceException;

import java.util.Arrays;
import java.util.List;

/**
 * Adapter para obtener información de segmentos.
 * 
 * OPTIMIZACIONES APLICADAS:
 * 
 * 1. CACHÉ de emails del segmento (10 minutos)
 *    - Los segmentos no cambian frecuentemente
 *    - Evita consultas repetidas a la BD durante preparación de campañas
 * 
 * 2. CACHÉ de conteo de miembros (10 minutos)
 *    - Útil para preview de destinatarios
 * 
 * 3. Fallback a BD si la API no está disponible
 *    - Resilencia ante fallos del servicio de segmentación
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
     * Resultado cacheado por 10 minutos.
     * 
     * FLUJO:
     * 1. Intenta obtener IDs vía API de segmentación
     * 2. Consulta emails en BD local
     * 3. Si falla API, usa fallback con JOIN directo
     */
    @Override
    @Cacheable(value = "mailing_segmento_emails", key = "#idSegmento", unless = "#result.isEmpty()")
    public List<String> obtenerEmailsSegmento(Long idSegmento) {
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║  OBTENIENDO EMAILS DEL SEGMENTO: {} (sin caché) ", idSegmento);
        log.info("╚══════════════════════════════════════════════════╝");
        
        try {
            // Paso 1: Obtener IDs de miembros via API
            List<Long> idsLeads = obtenerIdsMiembrosPorApi(idSegmento);
            
            if (idsLeads == null || idsLeads.isEmpty()) {
                log.warn("  ⚠ El segmento {} no tiene miembros", idSegmento);
                return List.of();
            }
            
            log.info("  📋 Obtenidos {} IDs de miembros", idsLeads.size());
            
            // Paso 2: Obtener emails de esos leads
            List<String> emails = obtenerEmailsPorIds(idsLeads);
            
            log.info("  ✓ {} emails obtenidos del segmento {}", emails.size(), idSegmento);
            return emails;
            
        } catch (Exception e) {
            log.warn("  ⚠ API no disponible, usando fallback: {}", e.getMessage());
            return obtenerEmailsPorBD(idSegmento);
        }
    }

    /**
     * Cuenta los miembros de un segmento.
     * Resultado cacheado por 10 minutos.
     */
    @Override
    @Cacheable(value = "mailing_segmento_count", key = "#idSegmento")
    public Integer contarMiembros(Long idSegmento) {
        log.debug("Contando miembros del segmento {} (sin caché)", idSegmento);
        
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
        return contarMiembrosPorBD(idSegmento);
    }

    // ========================================================================
    // MÉTODOS PRIVADOS
    // ========================================================================

    /**
     * Llama al endpoint /api/v1/internal/segmentos/{id}/miembros
     */
    private List<Long> obtenerIdsMiembrosPorApi(Long idSegmento) {
        String url = segmentacionUrl + "/api/v1/internal/segmentos/" + idSegmento + "/miembros";
        
        log.debug("  Llamando API: {}", url);
        
        try {
            Long[] ids = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Long[].class);
            
            return ids != null ? Arrays.asList(ids) : List.of();
            
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
     */
    private List<String> obtenerEmailsPorBD(Long idSegmento) {
        log.info("  📂 Usando fallback: consulta directa a BD");
        
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
            log.error("  ✗ Error obteniendo emails por BD: {}", e.getMessage());
            throw new ExternalServiceException("Segmentacion", 
                "No se pudieron obtener emails del segmento: " + e.getMessage());
        }
    }

    /**
     * Cuenta miembros directamente en BD
     */
    private Integer contarMiembrosPorBD(Long idSegmento) {
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
















