package pe.unmsm.crm.marketing.campanas.mailing.infra.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.IGestorCampanaPort;
import pe.unmsm.crm.marketing.shared.infra.exception.ExternalServiceException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GestorCampanaAdapter implements IGestorCampanaPort {

    private final RestClient restClient;

    @Value("${app.gestor.url:http://localhost:8080}")
    private String gestorUrl;

    @Override
    public void pausarCampana(Long idCampanaGestion, String motivo) {
        try {
            log.warn("Notificando al Gestor para pausar campaña: {}", idCampanaGestion);
            
            String url = gestorUrl + "/api/v1/campanas/" + idCampanaGestion + "/pausar";
            
            Map<String, String> payload = new HashMap<>();
            payload.put("motivo", motivo);
            
            restClient.post()
                    .uri(url)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("✓ Gestor notificado de pausa para campaña {}", idCampanaGestion);
            
        } catch (RestClientException e) {
            log.error("Error al notificar pausa al Gestor: {}", e.getMessage());
            throw new ExternalServiceException("Gestor", 
                "No se pudo notificar la pausa: " + e.getMessage());
        }
    }
}