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
import pe.unmsm.crm.marketing.shared.logging.AuditoriaService;
import pe.unmsm.crm.marketing.shared.logging.ModuloLog;
import pe.unmsm.crm.marketing.shared.logging.AccionLog;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LeadManagementService {

    private final LeadRepository leadRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditoriaService auditoriaService;

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

        // AUDITORÍA: Registrar cambio de estado
        auditoriaService.registrarEvento(
                ModuloLog.LEADS,
                AccionLog.CAMBIAR_ESTADO,
                leadId,
                null, // TODO: Agregar ID de usuario cuando esté disponible en el contexto de
                      // seguridad
                String.format("Estado cambiado de %s a %s. Motivo: %s",
                        estadoAnterior, nuevoEstado, motivo != null ? motivo : "Cambio manual"));
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

        // AUDITORÍA: Registrar eliminación
        auditoriaService.registrarEvento(
                ModuloLog.LEADS,
                AccionLog.ELIMINAR,
                id,
                null, // TODO: Agregar ID de usuario cuando esté disponible
                "Lead eliminado");
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

        // AUDITORÍA: Registrar eliminación en lote
        if (eliminados > 0) {
            auditoriaService.registrarEvento(
                    ModuloLog.LEADS,
                    AccionLog.ELIMINAR,
                    null, // Operación en lote, no hay un solo ID
                    null, // TODO: Agregar ID de usuario
                    String.format("Eliminación en lote: %d leads eliminados de %d solicitados",
                            eliminados, ids.size()));
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

        // AUDITORÍA: Registrar calificación en lote
        if (actualizados > 0) {
            auditoriaService.registrarEvento(
                    ModuloLog.LEADS,
                    AccionLog.CAMBIAR_ESTADO,
                    null, // Operación en lote
                    null, // TODO: Agregar ID de usuario
                    String.format(
                            "Calificación en lote: %d leads actualizados a estado %s de %d solicitados. Motivo: %s",
                            actualizados, nuevoEstado, ids.size(), motivo != null ? motivo : "No especificado"));
        }

        return actualizados;
    }

    public List<Lead> obtenerLeadsPorIds(@NonNull List<@NonNull Long> ids) {
        return leadRepository.findAllById(ids);
    }
}
