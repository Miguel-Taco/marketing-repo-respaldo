package pe.unmsm.crm.marketing.campanas.mailing.infra.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.ISegmentoPort;
import pe.unmsm.crm.marketing.shared.infra.exception.ExternalServiceException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SegmentoAdapter implements ISegmentoPort {

    private final RestClient restClient;

    @Value("${app.segmentacion.url:http://localhost:8080}")
    private String segmentacionUrl;

    @Override
    public List<String> obtenerEmailsSegmento(Long idSegmento) {
        try {
            log.info("Obteniendo emails del segmento: {}", idSegmento);
            
            String url = segmentacionUrl + "/api/v1/internal/segmentos/" + idSegmento + "/emails";
            
            String[] emails = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String[].class);
            
            if (emails == null) {
                log.warn("No se obtuvieron emails del segmento {}", idSegmento);
                return List.of();
            }
            
            log.info("✓ {} emails obtenidos del segmento {}", emails.length, idSegmento);
            return Arrays.asList(emails);
            
        } catch (RestClientException e) {
            log.error("Error al consultar segmento {}: {}", idSegmento, e.getMessage());
            throw new ExternalServiceException("Segmentacion", 
                "No se pudo obtener emails del segmento: " + e.getMessage());
        }
    }

    @Override
    public Integer contarMiembros(Long idSegmento) {
        try {
            String url = segmentacionUrl + "/api/v1/internal/segmentos/" + idSegmento + "/count";
            
            Integer count = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Integer.class);
            
            return count != null ? count : 0;
            
        } catch (RestClientException e) {
            log.error("Error al contar miembros del segmento {}: {}", idSegmento, e.getMessage());
            throw new ExternalServiceException("Segmentacion", 
                "No se pudo contar miembros del segmento");
        }
    }
}