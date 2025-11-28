package pe.unmsm.crm.marketing.campanas.telefonicas.domain.state;

import pe.unmsm.crm.marketing.campanas.telefonicas.domain.model.CallStatus;

import java.util.Map;
import java.util.Set;

/**
 * Maquina de estados simple para llamadas.
 */
public class CallStateMachine {

    private static final Map<CallStatus, Set<CallStatus>> ALLOWED = Map.of(
            CallStatus.PENDIENTE, Set.of(CallStatus.EN_LLAMADA, CallStatus.REAGENDADO, CallStatus.CERRADO, CallStatus.CANCELADO),
            CallStatus.EN_LLAMADA, Set.of(CallStatus.REAGENDADO, CallStatus.CERRADO, CallStatus.CANCELADO),
            CallStatus.REAGENDADO, Set.of(CallStatus.EN_LLAMADA, CallStatus.CERRADO, CallStatus.CANCELADO),
            CallStatus.CERRADO, Set.of(),
            CallStatus.CANCELADO, Set.of()
    );

    public boolean canTransition(CallStatus from, CallStatus to) {
        if (from == null || to == null) return false;
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }
}

