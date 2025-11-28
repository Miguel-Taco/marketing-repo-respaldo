package pe.unmsm.crm.marketing.campanas.telefonicas.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Intento de llamada programado o ejecutado.
 */
@Value
@Builder
public class CallAttempt {
    Long intentoNumero;
    Long campaniaId;
    Long contactoId;
    Long agenteId;
    LocalDateTime programadoPara;
    LocalDateTime ejecutadoEn;
    CallOutcome resultado;
    String motivo;
}

