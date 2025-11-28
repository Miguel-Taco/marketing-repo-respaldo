package pe.unmsm.crm.marketing.campanas.telefonicas.domain.handler;

import lombok.extern.slf4j.Slf4j;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.ResultadoLlamadaRequest;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallContext;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallOutcome;

/**
 * Maneja derivación a ventas.
 */
@Slf4j
public class DerivarAVentasHandler extends AbstractCallResultHandler {
    @Override
    protected boolean process(ResultadoLlamadaRequest request, CallContext context) {
        if (!CallOutcome.DERIVADO_VENTAS.name().equalsIgnoreCase(request.getResultado())
                && !CallOutcome.VENTA.name().equalsIgnoreCase(request.getResultado())) {
            return false;
        }
        log.info("Derivando/registrando venta para contacto {} en campania {}", context.getContactoId(), context.getCampaniaId());
        return true;
    }
}

