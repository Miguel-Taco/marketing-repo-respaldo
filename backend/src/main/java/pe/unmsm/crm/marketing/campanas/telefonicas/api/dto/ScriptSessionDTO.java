package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

@Value
@Builder
public class ScriptSessionDTO {
    Long llamadaId;
    Long agenteId;
    int pasoActual;
    Map<String, String> respuestas;
    LocalDateTime actualizadoEn;
}

