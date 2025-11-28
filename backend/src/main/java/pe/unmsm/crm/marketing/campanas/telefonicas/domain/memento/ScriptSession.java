package pe.unmsm.crm.marketing.campanas.telefonicas.domain.memento;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Originador de la sesión de guion interactivo.
 */
@Getter
@Builder
public class ScriptSession {
    private final Long llamadaId;
    private final Long agenteId;
    private int pasoActual;
    private Map<String, String> respuestas;
    private LocalDateTime actualizadoEn;

    public ScriptSessionMemento createMemento() {
        return ScriptSessionMemento.builder()
                .llamadaId(llamadaId)
                .agenteId(agenteId)
                .pasoActual(pasoActual)
                .respuestas(respuestas)
                .actualizadoEn(LocalDateTime.now())
                .build();
    }

    public void restore(ScriptSessionMemento memento) {
        this.pasoActual = memento.getPasoActual();
        this.respuestas = memento.getRespuestas();
        this.actualizadoEn = memento.getActualizadoEn();
    }
}
