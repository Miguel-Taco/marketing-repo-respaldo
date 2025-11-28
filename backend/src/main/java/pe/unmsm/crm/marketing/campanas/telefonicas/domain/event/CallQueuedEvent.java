package pe.unmsm.crm.marketing.campanas.telefonicas.domain.event;

import lombok.Value;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.ContactoDTO;

@Value
public class CallQueuedEvent {
    Long campaniaId;
    ContactoDTO contacto;
}

