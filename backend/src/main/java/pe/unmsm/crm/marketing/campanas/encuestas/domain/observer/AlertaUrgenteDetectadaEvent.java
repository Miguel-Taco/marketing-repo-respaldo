package pe.unmsm.crm.marketing.campanas.encuestas.domain.observer;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Evento que se publica cuando se detecta una alerta urgente en una respuesta
 * de encuesta.
 * Implementa el patrón Observer mediante Spring Events.
 * 
 * Este evento permite desacoplar el módulo de encuestas del módulo de
 * telemarketing.
 * Cualquier componente puede escuchar este evento sin que el publicador lo
 * sepa.
 */
@Getter
public class AlertaUrgenteDetectadaEvent {

    private final Long idLead;
    private final Integer idEncuesta;
    private final LocalDateTime timestamp;

    public AlertaUrgenteDetectadaEvent(Long idLead, Integer idEncuesta) {
        this.idLead = idLead;
        this.idEncuesta = idEncuesta;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("AlertaUrgenteDetectadaEvent[idLead=%d, idEncuesta=%d, timestamp=%s]",
                idLead, idEncuesta, timestamp);
    }
}
