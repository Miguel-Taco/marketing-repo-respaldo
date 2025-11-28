package pe.unmsm.crm.marketing.leads.application.observer;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.leads.domain.event.LeadEstadoCambiadoEvent;
import pe.unmsm.crm.marketing.leads.domain.model.HistorialEstadoLead;
import pe.unmsm.crm.marketing.leads.domain.repository.HistorialRepository;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class LeadHistorialObserver {

    private final HistorialRepository historialRepository;

    @EventListener
    public void alCambiarEstado(LeadEstadoCambiadoEvent evento) {
        HistorialEstadoLead historial = HistorialEstadoLead.builder()
                .leadId(evento.getLeadId())
                .estadoAnterior(evento.getEstadoAnterior())
                .estadoNuevo(evento.getEstadoNuevo())
                .fechaCambio(LocalDateTime.now())
                .motivo(evento.getMotivo())
                .build();

        historialRepository.save(Objects.requireNonNull(historial, "Historial no puede ser null"));
        System.out.println("OBSERVER: Auditoría registrada para Lead ID " + evento.getLeadId());
    }
}
