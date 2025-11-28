package pe.unmsm.crm.marketing.campanas.telefonicas.domain.state;

import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.LeadStatus;

import java.util.Map;
import java.util.Set;

/**
 * Maquina de estados simple para leads dentro de la campana.
 */
public class LeadStateMachine {

    private static final Map<LeadStatus, Set<LeadStatus>> ALLOWED = Map.of(
        LeadStatus.PENDIENTE, Set.of(LeadStatus.EN_LLAMADA, LeadStatus.REAGENDADO, LeadStatus.CERRADO_EXITOSO, LeadStatus.DESCARTADO),
        LeadStatus.EN_LLAMADA, Set.of(LeadStatus.REAGENDADO, LeadStatus.CERRADO_EXITOSO, LeadStatus.DESCARTADO),
        LeadStatus.REAGENDADO, Set.of(LeadStatus.EN_LLAMADA, LeadStatus.CERRADO_EXITOSO, LeadStatus.DESCARTADO),
        LeadStatus.CERRADO_EXITOSO, Set.of(),
        LeadStatus.DESCARTADO, Set.of()
    );

    public boolean canTransition(LeadStatus from, LeadStatus to) {
        if (from == null || to == null) return false;
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }
}

