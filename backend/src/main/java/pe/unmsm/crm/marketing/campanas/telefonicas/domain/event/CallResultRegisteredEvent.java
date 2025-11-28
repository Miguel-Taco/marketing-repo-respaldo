package pe.unmsm.crm.marketing.campanas.telefonicas.domain.event;

import lombok.Value;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.LlamadaDTO;

@Value
public class CallResultRegisteredEvent {
    Long campaniaId;
    Long agenteId;
    LlamadaDTO llamada;
}

