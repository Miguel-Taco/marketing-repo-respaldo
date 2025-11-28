package pe.unmsm.crm.marketing.campanas.telefonicas.domain.memento;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

@Value
@Builder
public class ScriptSessionMemento {
    Long llamadaId;
    Long agenteId;
    int pasoActual;
    Map<String, String> respuestas;
    LocalDateTime actualizadoEn;
}
