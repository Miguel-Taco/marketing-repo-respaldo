package pe.unmsm.crm.marketing.campanas.telefonicas.domain.handler;

import lombok.extern.slf4j.Slf4j;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.ResultadoLlamadaRequest;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallContext;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallOutcome;

/**
 * Maneja casos en los que el contacto no contesta.
 */
@Slf4j
public class NoContestaHandler extends AbstractCallResultHandler {
    @Override
    protected boolean process(ResultadoLlamadaRequest request, CallContext context) {
        if (!CallOutcome.NO_CONTESTA.name().equalsIgnoreCase(request.getResultado())) {
            return false;
        }
        log.info("Resultado NO_CONTESTA para contacto {} en campania {}", context.getContactoId(), context.getCampaniaId());
        return true;
    }
}

