package pe.unmsm.crm.marketing.campanas.telefonicas.domain.handler;

import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.ResultadoLlamadaRequest;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallContext;

/**
 * Handler de resultado de llamada dentro de una cadena de responsabilidad.
 */
public interface CallResultHandler {
    /**
     * Procesa el resultado. Devuelve true si manejó el resultado; false para pasar al siguiente.
     */
    boolean handle(ResultadoLlamadaRequest request, CallContext context);

    /**
     * Define el siguiente handler en la cadena.
     */
    void setNext(CallResultHandler next);
}

