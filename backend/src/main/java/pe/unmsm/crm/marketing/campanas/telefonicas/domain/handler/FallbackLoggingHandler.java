package pe.unmsm.crm.marketing.campanas.telefonicas.domain.handler;

import lombok.extern.slf4j.Slf4j;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.ResultadoLlamadaRequest;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallContext;

/**
 * Handler final de logging para cualquier resultado no manejado.
 */
@Slf4j
public class FallbackLoggingHandler extends AbstractCallResultHandler {
    @Override
    protected boolean process(ResultadoLlamadaRequest request, CallContext context) {
        log.info("Resultado {} no manejado especificamente para contacto {} en campania {}", request.getResultado(), context.getContactoId(), context.getCampaniaId());
        return true;
    }
}

