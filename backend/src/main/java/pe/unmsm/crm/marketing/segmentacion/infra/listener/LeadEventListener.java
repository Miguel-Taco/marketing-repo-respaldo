package pe.unmsm.crm.marketing.segmentacion.infra.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.unmsm.crm.marketing.leads.domain.event.LeadEstadoCambiadoEvent;
import pe.unmsm.crm.marketing.leads.domain.event.LeadEliminadoEvent;
import pe.unmsm.crm.marketing.segmentacion.infra.cache.LeadCacheService;

/**
 * Listener de eventos de Leads para actualizar el caché de segmentación
 * de forma incremental (sin recargar todo el caché)
 */
@Component
@RequiredArgsConstructor
@Slf4j
@org.springframework.context.annotation.Profile("!console")
public class LeadEventListener {

    private final LeadCacheService leadCacheService;

    /**
     * Escucha eventos de cambio de estado de leads (creación/actualización)
     * y actualiza el lead en el caché.
     * Se ejecuta DESPUÉS del commit para asegurar que el lead ya existe en BD.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLeadEstadoCambiado(LeadEstadoCambiadoEvent event) {
        log.info(" [CACHE] Evento recibido: Lead ID {} cambió de {} a {}",
                event.getLeadId(), event.getEstadoAnterior(), event.getEstadoNuevo());

        // Actualizar el lead en el caché (o agregarlo si es nuevo)
        leadCacheService.updateLeadInCache(event.getLeadId());
    }

    /**
     * Escucha eventos de eliminación de leads y los remueve del caché.
     * Se ejecuta DESPUÉS del commit para asegurar que la eliminación es definitiva.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLeadEliminado(LeadEliminadoEvent event) {
        log.info(" [CACHE] Evento recibido: Lead ID {} eliminado", event.getLeadId());

        // Remover el lead del caché
        leadCacheService.removeLeadFromCache(event.getLeadId());
    }
}
