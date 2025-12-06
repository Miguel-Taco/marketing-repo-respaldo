package pe.unmsm.crm.marketing.campanas.telefonicas.domain.event;

import lombok.Value;

/**
 * Evento publicado cuando se actualiza la configuración de una campaña.
 * Afecta cachés: campaign
 */
@Value
public class CampaignConfigUpdatedEvent {
    Long campaniaId;
}
