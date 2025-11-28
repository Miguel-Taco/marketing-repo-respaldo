package pe.unmsm.crm.marketing.campanas.telefonicas.domain.handler;

import lombok.extern.slf4j.Slf4j;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.ResultadoLlamadaRequest;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallContext;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallOutcome;

/**
 * Maneja contactos exitosos (INTERESADO o CONTACTADO).
 */
@Slf4j
public class ContactoExitosoHandler extends AbstractCallResultHandler {
    @Override
    protected boolean process(ResultadoLlamadaRequest request, CallContext context) {
        if (!CallOutcome.INTERESADO.name().equalsIgnoreCase(request.getResultado())
                && !CallOutcome.CONTACTADO.name().equalsIgnoreCase(request.getResultado())) {
            return false;
        }
        log.info("Resultado exitoso {} para contacto {} en campania {}", request.getResultado(), context.getContactoId(), context.getCampaniaId());
        return true;
    }
}

