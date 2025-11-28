package pe.unmsm.crm.marketing.campanas.telefonicas.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Resultado de aplicar una estrategia de asignación de contacto a agente.
 */
@Value
@Builder
public class LeadAssignment {
    Long campaniaId;
    Long contactoId;
    Long agenteId;
    AssignmentStrategyType estrategia;
}

