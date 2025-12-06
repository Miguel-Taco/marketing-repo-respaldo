package pe.unmsm.crm.marketing.campanas.telefonicas.domain.event;

import lombok.Value;
import java.util.List;

/**
 * Evento publicado cuando se actualiza un guion estructurado.
 * Afecta cachés: guion en TODAS las campañas que usan este guion.
 */
@Value
public class ScriptUpdatedEvent {
    Integer guionId;
    List<Long> affectedCampaignIds; // Campañas que usan este guion
}
