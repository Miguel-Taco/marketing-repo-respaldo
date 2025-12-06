package pe.unmsm.crm.marketing.campanas.telefonicas.domain.event;

import lombok.Value;

/**
 * Evento publicado cuando se pausa o reanuda la cola de una campaña.
 * Afecta cachés: queue
 */
@Value
public class QueueToggledEvent {
    Long campaniaId;
    String action; // "PAUSED" o "RESUMED"
}
