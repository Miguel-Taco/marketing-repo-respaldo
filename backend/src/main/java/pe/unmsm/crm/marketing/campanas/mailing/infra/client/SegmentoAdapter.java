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

import java.util.Arrays;
import java.util.List;

/**
 * Adapter para obtener información de segmentos.
 * 
 * IMPORTANTE: Este adapter tiene DOS modos de operación:
 * 
 * 1. MODO API (recomendado): Si el endpoint /api/v1/internal/segmentos/{id}/emails existe
 * 2. MODO BD (temporal): Consulta directa a la BD mientras no exista el endpoint
 * 
 * El modo se selecciona automáticamente: intenta API primero, si falla usa BD.
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
     * Intenta primero por API, si falla usa consulta directa a BD.
     */
    @Override
    public List<String> obtenerEmailsSegmento(Long idSegmento) {
        log.info("Obteniendo emails del segmento: {}", idSegmento);
        
        // Intentar por API primero
        try {
            return obtenerEmailsPorApi(idSegmento);
        } catch (Exception e) {
            log.warn("API de segmentos no disponible, usando consulta directa a BD: {}", e.getMessage());
            return obtenerEmailsPorBD(idSegmento);
        }
    }

    /**
     * Modo API: Llama al endpoint del módulo de Segmentación
     * 
     * NOTA: Este endpoint DEBE ser creado por tu compañero de Segmentación:
     * GET /api/v1/internal/segmentos/{id}/emails
     * Retorna: List<String> con los emails
     */
    private List<String> obtenerEmailsPorApi(Long idSegmento) {
        String url = segmentacionUrl + "/api/v1/internal/segmentos/" + idSegmento + "/emails";
        
        log.debug("Llamando API: {}", url);
        
        String[] emails = restClient.get()
                .uri(url)
                .retrieve()
                .body(String[].class);
        
        if (emails == null) {
            log.warn("API retornó null para segmento {}", idSegmento);
            return List.of();
        }
        
        log.info("✓ {} emails obtenidos del segmento {} vía API", emails.length, idSegmento);
        return Arrays.asList(emails);
    }

    /**
     * Modo BD (temporal): Consulta directa a las tablas de segmentación
     * 
     * ESTRUCTURA:
     * - segmento_miembro: id_segmento, id_miembro (lead_id)
     * - leads: lead_id, email
     * 
     * JOIN para obtener los emails de los miembros del segmento
     */
    private List<String> obtenerEmailsPorBD(Long idSegmento) {
        log.info("Obteniendo emails por consulta directa a BD para segmento {}", idSegmento);
        
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
            
            log.info("✓ {} emails obtenidos del segmento {} vía BD", emails.size(), idSegmento);
            return emails;
            
        } catch (Exception e) {
            log.error("Error obteniendo emails por BD: {}", e.getMessage());
            throw new ExternalServiceException("Segmentacion", 
                "No se pudieron obtener emails del segmento: " + e.getMessage());
        }
    }

    /**
     * Cuenta los miembros de un segmento
     */
    @Override
    public Integer contarMiembros(Long idSegmento) {
        // Intentar por API
        try {
            return contarMiembrosPorApi(idSegmento);
        } catch (Exception e) {
            log.warn("API no disponible para contar, usando BD: {}", e.getMessage());
            return contarMiembrosPorBD(idSegmento);
        }
    }
    
    private Integer contarMiembrosPorApi(Long idSegmento) {
        // El endpoint de resumen ya existe
        String url = segmentacionUrl + "/api/v1/internal/segmentos/" + idSegmento + "/resumen";
        
        try {
            // El resumen incluye cantidadMiembros
            var response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(java.util.Map.class);
            
            if (response != null && response.containsKey("cantidadMiembros")) {
                return (Integer) response.get("cantidadMiembros");
            }
            return 0;
            
        } catch (RestClientException e) {
            throw new ExternalServiceException("Segmentacion", "Error contando miembros");
        }
    }
    
    private Integer contarMiembrosPorBD(Long idSegmento) {
        String sql = "SELECT COUNT(*) FROM segmento_miembro WHERE id_segmento = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idSegmento);
        return count != null ? count : 0;
    }
}