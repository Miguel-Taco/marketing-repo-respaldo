package pe.unmsm.crm.marketing.campanas.telefonicas.domain.event;

import lombok.Value;

/**
 * Evento publicado cuando se elimina una grabación.
 * Solo actualiza estado local en RecordingsPage si está abierta.
 */
@Value
public class RecordingDeletedEvent {
    Long grabacionId;
}
