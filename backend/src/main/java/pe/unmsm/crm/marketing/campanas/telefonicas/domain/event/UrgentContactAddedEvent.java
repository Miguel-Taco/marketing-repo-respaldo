package pe.unmsm.crm.marketing.campanas.telefonicas.domain.event;

import lombok.Value;

/**
 * Evento publicado cuando se agrega un contacto urgente a la cola.
 * Afecta cachés: queue, leads
 */
@Value
public class UrgentContactAddedEvent {
    Long campaniaId;
    Long leadId;
    String prioridad;
}
