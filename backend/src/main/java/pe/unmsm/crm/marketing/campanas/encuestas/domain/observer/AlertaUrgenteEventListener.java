package pe.unmsm.crm.marketing.campanas.encuestas.domain.observer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener que escucha eventos de alertas urgentes detectadas en encuestas.
 * Implementa el patrón Observer mediante Spring Events.
 * 
 * Cuando se detecta una alerta urgente, este listener notifica al módulo de
 * telemarketing
 * para agregar el lead a la cola de llamadas con prioridad alta.
 * 
 * El listener está desacoplado del publicador - el servicio de respuestas no
 * sabe
 * que este listener existe.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertaUrgenteEventListener {

    private final RestTemplate restTemplate;

    @Value("${telemarketing.api.base-url:http://localhost:8080}")
    private String telemarketingBaseUrl;

    @Value("${telemarketing.api.urgent-queue-endpoint:/api/v1/public/v1/campanias-telefonicas/cola/urgente}")
    private String urgentQueueEndpoint;

    /**
     * Escucha eventos de alertas urgentes y notifica al módulo de telemarketing.
     * 
     * Usa @TransactionalEventListener para ejecutarse después del commit de la
     * transacción,
     * garantizando que la respuesta ya está guardada en la BD antes de notificar.
     * 
     * @param event Evento con información del lead y encuesta
     */
    @TransactionalEventListener
    public void handleAlertaUrgenteDetectada(AlertaUrgenteDetectadaEvent event) {
        log.info("Evento recibido: {}", event);

        try {
            String url = telemarketingBaseUrl + urgentQueueEndpoint;

            // Preparar el cuerpo de la petición
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("idLead", event.getIdLead());
            requestBody.put("idEncuesta", event.getIdEncuesta());

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Realizar la llamada POST
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, request,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Lead {} agregado a cola urgente exitosamente para encuesta {}",
                        event.getIdLead(), event.getIdEncuesta());
            } else {
                log.warn("Respuesta inesperada al agregar lead {} a cola urgente: {}",
                        event.getIdLead(), response.getStatusCode());
            }

        } catch (Exception e) {
            // No lanzamos la excepción para no afectar el flujo principal
            // Solo registramos el error
            log.error("Error al notificar alerta urgente para lead {} y encuesta {}: {}",
                    event.getIdLead(), event.getIdEncuesta(), e.getMessage(), e);
        }
    }
}
