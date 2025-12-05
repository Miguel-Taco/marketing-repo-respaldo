package pe.unmsm.crm.marketing.leads.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Evento publicado cuando un lead es eliminado del sistema.
 * Permite a otros módulos (ej. Segmentación) actualizar sus cachés.
 */
@Getter
public class LeadEliminadoEvent extends ApplicationEvent {

    private final Long leadId;
    private final String usuarioId; // Opcional: para auditoría

    public LeadEliminadoEvent(Object source, Long leadId, String usuarioId) {
        super(source);
        this.leadId = leadId;
        this.usuarioId = usuarioId;
    }

    public LeadEliminadoEvent(Object source, Long leadId) {
        this(source, leadId, null);
    }
}
