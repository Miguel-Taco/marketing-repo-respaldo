package pe.unmsm.crm.marketing.campanas.telefonicas.domain.event;

import lombok.Value;

/**
 * Evento publicado cuando un agente toma un contacto de la cola.
 * Afecta cachés: queue, leads, dailyMetrics
 */
@Value
public class ContactTakenEvent {
    Long campaniaId;
    Long agenteId;
    Long contactoId;
}
