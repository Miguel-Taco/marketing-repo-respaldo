package pe.unmsm.crm.marketing.campanas.gestor.infra.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.IConsultaRecursosPort;

import java.time.LocalDateTime;

/**
 * Adaptador HTTP para consultar la disponibilidad de segmentos
 * en el módulo de Segmentación.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SegmentoClientAdapter {

    private final RestClient restClient;

    @Value("${app.segmentacion.url:http://localhost:8080}")
    private String segmentacionBaseUrl;

    /**
     * Verifica si un segmento existe, está activo y tiene miembros.
     * Consume: GET /api/v1/internal/segmentos/{id}/resumen
     */
    public boolean existeSegmento(Long idSegmento) {
        if (idSegmento == null) {
            return false;
        }

        try {
            String url = segmentacionBaseUrl + "/api/v1/internal/segmentos/" + idSegmento + "/resumen";

            SegmentoResumenDto resumen = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(SegmentoResumenDto.class);

            if (resumen == null) {
                log.warn("No se pudo obtener el resumen del segmento {}", idSegmento);
                return false;
            }

            // Validación: el segmento debe estar ACTIVO y tener miembros
            boolean esValido = "ACTIVO".equals(resumen.estado()) && resumen.cantidadMiembros() > 0;

            if (!esValido) {
                log.warn("Segmento {} no válido. Estado: {}, Miembros: {}",
                        idSegmento, resumen.estado(), resumen.cantidadMiembros());
            }

            return esValido;

        } catch (RestClientException e) {
            log.error("Error al consultar el segmento {}: {}", idSegmento, e.getMessage());
            return false;
        }
    }

    /**
     * DTO para recibir el resumen del segmento desde la API interna
     */
    private record SegmentoResumenDto(
            Long id,
            String nombre,
            String descripcion,
            String tipoAudiencia,
            Integer cantidadMiembros,
            String estado) {
    }
}
