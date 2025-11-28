package pe.unmsm.crm.marketing.campanas.telefonicas.domain.handler;

import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.ResultadoLlamadaRequest;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallContext;

/**
 * Base que encadena handlers y delega cuando no procesa.
 */
public abstract class AbstractCallResultHandler implements CallResultHandler {

    private CallResultHandler next;

    @Override
    public void setNext(CallResultHandler next) {
        this.next = next;
    }

    @Override
    public boolean handle(ResultadoLlamadaRequest request, CallContext context) {
        if (process(request, context)) {
            return true;
        }
        if (next != null) {
            return next.handle(request, context);
        }
        return false;
    }

    /**
     * Implementa la lógica específica del handler.
     */
    protected abstract boolean process(ResultadoLlamadaRequest request, CallContext context);
}

