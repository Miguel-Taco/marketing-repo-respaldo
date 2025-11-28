package pe.unmsm.crm.marketing.campanas.telefonicas.domain.handler;

import lombok.extern.slf4j.Slf4j;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.ResultadoLlamadaRequest;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallContext;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallOutcome;

/**
 * Maneja cuando la llamada cae a buzón de voz.
 */
@Slf4j
public class BuzonVozHandler extends AbstractCallResultHandler {
    @Override
    protected boolean process(ResultadoLlamadaRequest request, CallContext context) {
        if (!CallOutcome.BUZON_VOZ.name().equalsIgnoreCase(request.getResultado())) {
            return false;
        }
        log.info("Resultado BUZON_VOZ para contacto {} en campania {}", context.getContactoId(), context.getCampaniaId());
        return true;
    }
}

