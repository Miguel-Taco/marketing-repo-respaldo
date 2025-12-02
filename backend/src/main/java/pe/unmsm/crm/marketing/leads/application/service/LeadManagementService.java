package pe.unmsm.crm.marketing.leads.application.service;

import lombok.RequiredArgsConstructor;
import lombok.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.unmsm.crm.marketing.leads.domain.event.LeadEstadoCambiadoEvent;
import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoLead;
import pe.unmsm.crm.marketing.leads.domain.repository.LeadRepository;
import pe.unmsm.crm.marketing.shared.infra.exception.BusinessException;
import pe.unmsm.crm.marketing.shared.infra.exception.NotFoundException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LeadManagementService {

    private final LeadRepository leadRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void cualificarLead(@NonNull Long leadId, @NonNull EstadoLead nuevoEstado, String motivo) {

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new NotFoundException("Lead", leadId));

        EstadoLead estadoAnterior = lead.getEstado();
        if (estadoAnterior == nuevoEstado)
            return;

        if (nuevoEstado == EstadoLead.CALIFICADO) {
            validarRequisitosCalificacion(lead);
        }

        lead.setEstado(nuevoEstado);
        leadRepository.save(lead);

        // PATRÓN OBSERVER: Notificamos el cambio
        eventPublisher.publishEvent(new LeadEstadoCambiadoEvent(
                lead.getId(), estadoAnterior, nuevoEstado, motivo != null ? motivo : "Cambio manual Agente"));
    }

    // Sobrecarga para mantener compatibilidad si es necesario, o eliminar si todos
    // usan la nueva firma
    @Transactional
    public void cualificarLead(@NonNull Long leadId, @NonNull EstadoLead nuevoEstado) {
        cualificarLead(leadId, nuevoEstado, null);
    }

    private void validarRequisitosCalificacion(Lead lead) {
        if (lead.getContacto() == null ||
                lead.getContacto().getTelefono() == null ||
                lead.getContacto().getTelefono().isBlank()) {
            throw new BusinessException("VALIDATION_ERROR", "Se requiere teléfono para calificar el lead.");
        }

        if (lead.getDemograficos() == null ||
                lead.getDemograficos().getDistrito() == null ||
                lead.getDemograficos().getDistrito().isBlank()) {
            throw new BusinessException("VALIDATION_ERROR", "Se requiere distrito para calificar el lead.");
        }
    }

    @Transactional
    public void eliminarLead(@NonNull Long id) {
        if (!leadRepository.existsById(id)) {
            throw new NotFoundException("Lead", id);
        }
        // Al borrar el Lead, la BD borrará el historial automáticamente por el ON
        // DELETE CASCADE
        leadRepository.deleteById(id);
    }

    public Lead findLeadById(@NonNull Long id) {
        return leadRepository.findById(id).orElse(null);
    }

    @Transactional
    public int eliminarLeadsEnLote(@NonNull List<@NonNull Long> ids) {
        int eliminados = 0;
        for (Long id : ids) {
            // Validación explícita de null para satisfacer el análisis de null-safety
            @NonNull
            Long nonNullId = Objects.requireNonNull(id, "El ID no puede ser null");
            if (leadRepository.existsById(nonNullId)) {
                leadRepository.deleteById(nonNullId);
                eliminados++;
            }
        }
        return eliminados;
    }

    @Transactional
    public int cualificarLeadsEnLote(@NonNull List<@NonNull Long> ids, @NonNull EstadoLead nuevoEstado, String motivo) {
        int actualizados = 0;
        for (Long id : ids) {
            // Validación explícita de null para satisfacer el análisis de null-safety
            @NonNull
            Long nonNullId = Objects.requireNonNull(id, "El ID no puede ser null");
            if (leadRepository.existsById(nonNullId)) {
                try {
                    cualificarLead(nonNullId, nuevoEstado, motivo);
                    actualizados++;
                } catch (Exception e) {
                    // Si falla uno, continuamos con los demás
                    // Podrías loggear el error aquí si es necesario
                }
            }
        }
        return actualizados;
    }

    public List<Lead> obtenerLeadsPorIds(@NonNull List<@NonNull Long> ids) {
        return leadRepository.findAllById(ids);
    }
}
