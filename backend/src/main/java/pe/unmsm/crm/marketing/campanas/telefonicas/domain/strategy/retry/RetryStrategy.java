package pe.unmsm.crm.marketing.campanas.telefonicas.domain.strategy.retry;

import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.ResultadoLlamadaRequest;
import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.RetryPlanStep;

import java.util.List;

/**
 * Estrategia para calcular reintentos de llamada.
 */
public interface RetryStrategy {
    List<RetryPlanStep> planRetries(ResultadoLlamadaRequest request, int intentoActual);
}
