package pe.unmsm.crm.marketing.leads.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoLead;
import pe.unmsm.crm.marketing.shared.domain.DomainEvent;

@Getter
@RequiredArgsConstructor
public class LeadEstadoCambiadoEvent extends DomainEvent {
    private final Long leadId;
    private final EstadoLead estadoAnterior;
    private final EstadoLead estadoNuevo;
    private final String motivo;

    @Override
    public String getType() {
        return "LEAD_ESTADO_CAMBIADO";
    }
}
