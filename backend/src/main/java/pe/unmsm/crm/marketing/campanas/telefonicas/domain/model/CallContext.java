package pe.unmsm.crm.marketing.campanas.telefonicas.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Contexto de una llamada que viaja entre comandos, handlers y observadores.
 */
@Value
@Builder
public class CallContext {
    Long campaniaId;
    Long contactoId;
    Long agenteId;
    Long llamadaId;
    CallStatus estado;
    CallOutcome resultadoEsperado;
    LocalDateTime programadaPara;
    Map<String, Object> metadata;
}

