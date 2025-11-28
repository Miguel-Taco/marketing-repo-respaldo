package pe.unmsm.crm.marketing.campanas.telefonicas.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Representa un paso en el plan de reintentos calculado por una estrategia.
 */
@Value
@Builder
public class RetryPlanStep {
    int intento;
    LocalDateTime programadoPara;
    RetryPolicyType politica;
}

