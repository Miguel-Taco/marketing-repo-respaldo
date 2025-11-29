package pe.unmsm.crm.marketing.campanas.mailing.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.SendGridWebhookRequest;
import pe.unmsm.crm.marketing.campanas.mailing.application.service.WebhookSendGridService;

@RestController
@RequestMapping("/api/mailing/v1")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookSendGridService webhookService;

    /**
     * POST /api/mailing/v1/interacciones/webhook
     * Recibe array de eventos desde SendGrid
     * SendGrid envía eventos en lotes
     */
    @PostMapping("/interacciones/webhook")
    public ResponseEntity<Void> procesarWebhookSendGrid(
            @RequestBody SendGridWebhookRequest[] eventos) {
        
        log.info("=== WEBHOOK RECIBIDO ===");
        log.info("Cantidad de eventos: {}", eventos != null ? eventos.length : 0);
        
        if (eventos == null || eventos.length == 0) {
            log.warn("Webhook sin eventos");
            return ResponseEntity.ok().build();
        }

        // Procesar cada evento
        for (SendGridWebhookRequest evento : eventos) {
            try {
                log.debug("Procesando evento: tipo={}, email={}, timestamp={}", 
                    evento.getEvent(), evento.getEmail(), evento.getTimestamp());
                
                webhookService.procesarEvento(evento);
                
            } catch (Exception e) {
                log.error("Error procesando evento individual: {}", e.getMessage(), e);
                // Continuar con siguientes eventos (resilencia)
            }
        }
        
        log.info("=== WEBHOOK PROCESADO ===");
        return ResponseEntity.ok().build();
    }
}