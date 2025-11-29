package pe.unmsm.crm.marketing.campanas.mailing.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.SendGridWebhookRequest;

@RestController
@RequestMapping("/api/mailing/v1")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    // TODO: Implementar en siguiente fase (procesamiento de eventos SendGrid)
    @PostMapping("/interacciones/webhook")
    public ResponseEntity<Void> procesarWebhookSendGrid(@RequestBody SendGridWebhookRequest[] events) {
        log.info("Webhook recibido con {} eventos", events.length);
        // Implementación próxima
        return ResponseEntity.ok().build();
    }
}