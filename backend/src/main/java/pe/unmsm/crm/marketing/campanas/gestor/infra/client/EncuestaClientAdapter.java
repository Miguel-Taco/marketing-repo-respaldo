package pe.unmsm.crm.marketing.campanas.gestor.infra.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adaptador HTTP para consultar la disponibilidad de encuestas.
 * NOTA: El módulo de Encuestas está en
 * pe.unmsm.crm.marketing.campanas.encuestas
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EncuestaClientAdapter {

    private final RestClient restClient;

    @Value("${app.encuestas.url:http://localhost:8080}")
    private String encuestasBaseUrl;

    /**
     * Verifica si una encuesta existe y está activa.
     * TODO: Implementar cuando el módulo de Encuestas tenga API interna.
     * 
     * Por ahora, retorna true si el ID no es null.
     */
    public boolean existeEncuesta(Integer idEncuesta) {
        if (idEncuesta == null) {
            return true; // Es opcional, si es null está bien
        }

        // TODO: Implementar llamada HTTP cuando la API de Encuestas esté lista
        // String url = encuestasBaseUrl + "/api/v1/internal/encuestas/" + idEncuesta;

        log.debug("Validación de encuesta {} (PLACEHOLDER - sin validación real)", idEncuesta);
        return true; // Placeholder
    }
}
