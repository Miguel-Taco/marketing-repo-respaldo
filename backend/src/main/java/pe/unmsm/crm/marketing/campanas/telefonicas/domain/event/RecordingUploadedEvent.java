package pe.unmsm.crm.marketing.campanas.telefonicas.domain.event;

import lombok.Value;

/**
 * Evento publicado cuando se sube una grabación de llamada.
 * NO invalida cachés de CampaignCacheContext (grabaciones tienen gestión
 * independiente).
 */
@Value
public class RecordingUploadedEvent {
    Long grabacionId;
    Long campaniaId;
    Long agenteId;
    Long leadId;
}
