package pe.unmsm.crm.marketing.campanas.gestor.infra.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;

/**
 * Adaptador HTTP para consultar la disponibilidad de agentes.
 * NOTA: Este es un placeholder. El módulo de Agentes aún no está implementado.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AgenteClientAdapter {

    private final RestClient restClient;

    @Value("${app.agentes.url:http://localhost:8080}")
    private String agentesBaseUrl;

    /**
     * Verifica si un agente está disponible en el rango de fechas especificado.
     * TODO: Implementar cuando el módulo de Agentes esté disponible.
     * 
     * Por ahora, retorna true (sin validación real).
     */
    public boolean isAgenteDisponible(Integer idAgente, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (idAgente == null) {
            return false;
        }

        // TODO: Implementar llamada HTTP cuando el módulo de Agentes esté listo
        // String url = agentesBaseUrl + "/api/v1/internal/agentes/" + idAgente +
        // "/disponibilidad";

        log.debug("Validación de agente {} (PLACEHOLDER - siempre retorna true)", idAgente);
        return true; // Placeholder
    }
}
