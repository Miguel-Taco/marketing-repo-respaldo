package pe.unmsm.crm.marketing.campanas.telefonicas.domain.event;

import lombok.Value;

/**
 * Evento publicado cuando se crea una nueva campaña telefónica.
 * Afecta: Lista global de campañas (CampaignsContext)
 */
@Value
public class CampaignCreatedEvent {
    Long campaniaId;
    String nombre;
}
