package pe.unmsm.crm.marketing.campanas.telefonicas.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.event.CallQueuedEvent;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.event.CallResultRegisteredEvent;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.event.MetricsUpdatedEvent;

/**
 * Notificador via WebSocket para eventos de telemarketing.
 */
@Component
@RequiredArgsConstructor
public class TelemarketingWsNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onCallQueued(CallQueuedEvent event) {
        messagingTemplate.convertAndSend("/topic/cola", event);
    }

    @EventListener
    public void onCallResult(CallResultRegisteredEvent event) {
        messagingTemplate.convertAndSend("/topic/llamadas", event);
    }

    @EventListener
    public void onMetricsUpdated(MetricsUpdatedEvent event) {
        messagingTemplate.convertAndSend("/topic/metricas", event);
    }
}

