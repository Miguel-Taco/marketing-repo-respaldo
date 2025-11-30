package pe.unmsm.crm.marketing.campanas.encuestas.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para notificar alertas urgentes al módulo de telemarketing.
 * Cuando una respuesta de encuesta contiene opciones marcadas como urgentes,
 * este servicio agrega el lead a la cola de llamadas con prioridad alta.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UrgentAlertNotifier {

    private final RestTemplate restTemplate;

    @Value("${telemarketing.api.base-url:http://localhost:8080}")
    private String telemarketingBaseUrl;

    @Value("${telemarketing.api.urgent-queue-endpoint:/api/v1/public/v1/campanias-telefonicas/cola/urgente}")
    private String urgentQueueEndpoint;

    /**
     * Notifica al módulo de telemarketing que un lead ha respondido una encuesta
     * con opciones de alerta urgente.
     * 
     * @param idLead     ID del lead que respondió la encuesta
     * @param idEncuesta ID de la encuesta respondida
     */
    public void notifyUrgentAlert(Long idLead, Integer idEncuesta) {
        try {
            String url = telemarketingBaseUrl + urgentQueueEndpoint;

            // Preparar el cuerpo de la petición
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("idLead", idLead);
            requestBody.put("idEncuesta", idEncuesta);

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
                        idLead, idEncuesta);
            } else {
                log.warn("Respuesta inesperada al agregar lead {} a cola urgente: {}",
                        idLead, response.getStatusCode());
            }

        } catch (Exception e) {
            // No lanzamos la excepción para no afectar el flujo principal
            // Solo registramos el error
            log.error("Error al notificar alerta urgente para lead {} y encuesta {}: {}",
                    idLead, idEncuesta, e.getMessage(), e);
        }
    }
}
