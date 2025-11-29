package pe.unmsm.crm.marketing.campanas.mailing.infra.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.IVentasPort;
import pe.unmsm.crm.marketing.shared.infra.exception.ExternalServiceException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class VentasAdapter implements IVentasPort {

    private final RestClient restClient;

    @Value("${app.ventas.url:http://localhost:8080}")
    private String ventasUrl;

    @Override
    public void derivarInteresado(Integer idCampanaMailingId, Integer idAgenteAsignado, 
                                   Long idLead, Long idSegmento, Long idCampanaGestion) {
        try {
            log.info("Derivando interesado a Ventas - Lead: {}, Campaña: {}", idLead, idCampanaMailingId);
            
            String url = ventasUrl + "/api/ventas/oportunidades";
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("idCampanaMailingId", idCampanaMailingId);
            payload.put("idCampanaGestion", idCampanaGestion);
            payload.put("idAgenteAsignado", idAgenteAsignado);
            payload.put("idLead", idLead);
            payload.put("idSegmento", idSegmento);
            
            restClient.post()
                    .uri(url)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("✓ Lead {} derivado a Ventas", idLead);
            
        } catch (RestClientException e) {
            log.error("Error al derivar a Ventas: {}", e.getMessage());
            // NO lanzar excepción para mantener resilencia
            // El webhook continúa aunque falle la derivación
            log.warn("Se continuó a pesar del error en derivación");
        }
    }
}