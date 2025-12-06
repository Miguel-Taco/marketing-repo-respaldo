package pe.unmsm.crm.marketing.campanas.telefonicas.domain.event;

import lombok.Value;

/**
 * Evento publicado cuando se vincula un guion a una campaña.
 * Afecta cachés: campaign, scripts, guion
 */
@Value
public class ScriptLinkedEvent {
    Long campaniaId;
    Integer guionId;
}
