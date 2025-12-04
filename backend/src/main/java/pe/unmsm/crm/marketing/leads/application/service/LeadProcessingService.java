package pe.unmsm.crm.marketing.leads.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Imports de tus carpetas organizadas
import pe.unmsm.crm.marketing.leads.domain.factory.LeadFactory;
import pe.unmsm.crm.marketing.leads.domain.strategy.DeduplicationStrategy; // <--- STRATEGY
import pe.unmsm.crm.marketing.leads.domain.event.LeadEstadoCambiadoEvent; // <--- EVENTO

import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.leads.domain.enums.TipoFuente;
import pe.unmsm.crm.marketing.leads.domain.repository.LeadRepository;
import pe.unmsm.crm.marketing.shared.infra.exception.DuplicateLeadException;
import pe.unmsm.crm.marketing.shared.logging.AuditoriaService;
import pe.unmsm.crm.marketing.shared.logging.ModuloLog;
import pe.unmsm.crm.marketing.shared.logging.AccionLog;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeadProcessingService {

    private final List<LeadFactory> factories;
    private final LeadRepository leadRepository;
    private final DeduplicationStrategy deduplicationStrategy; // Inyección Strategy
    private final ApplicationEventPublisher eventPublisher; // Publicador de Eventos
    private final AuditoriaService auditoriaService;

    @Transactional
    public void procesarDesdeStaging(TipoFuente tipo, Object datoStaging) {
        LeadFactory factory = factories.stream()
                .filter(f -> f.soporta(tipo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No factory for: " + tipo));

        Lead leadEntrante = factory.convertirALead(datoStaging);

        // USO DEL PATRÓN STRATEGY (Delegamos la búsqueda)
        Optional<Lead> leadExistenteOpt = deduplicationStrategy.encontrarDuplicado(leadEntrante);

        if (leadExistenteOpt.isPresent()) {
            Lead existente = leadExistenteOpt.get();

            // Determinar si el duplicado es por email o teléfono
            String field = "email";
            String value = leadEntrante.getContacto().getEmail();

            if (existente.getContacto().getTelefono() != null &&
                    leadEntrante.getContacto().getTelefono() != null &&
                    existente.getContacto().getTelefono().equals(leadEntrante.getContacto().getTelefono())) {
                field = "teléfono";
                value = leadEntrante.getContacto().getTelefono();
            }

            // Lanzar excepción de duplicado
            // - Para WEB: será capturada por GlobalExceptionHandler -> HTTP 409
            // - Para IMPORTACION: será capturada por ImportService -> RECHAZADO
            throw new DuplicateLeadException(field, value);
        } else {
            @SuppressWarnings("null")
            Lead nuevoLead = Objects.requireNonNull(
                    leadRepository.save(leadEntrante),
                    "El lead guardado no puede ser null");
            // Publicamos evento con el estado REAL del lead (Observer implícito)
            // Para importados será CALIFICADO, para web será NUEVO
            eventPublisher.publishEvent(new LeadEstadoCambiadoEvent(
                    nuevoLead.getId(), null, nuevoLead.getEstado(), "Creación inicial (" + tipo + ")"));
            System.out.println("Lead creado: " + nuevoLead.getId() + " con estado: " + nuevoLead.getEstado());

            // AUDITORÍA: Registrar procesamiento exitoso
            auditoriaService.registrarEvento(
                    ModuloLog.LEADS,
                    AccionLog.CREAR,
                    nuevoLead.getId(),
                    null,
                    String.format("Lead procesado desde staging. Tipo: %s, Estado inicial: %s, Email: %s",
                            tipo, nuevoLead.getEstado(),
                            nuevoLead.getContacto() != null ? nuevoLead.getContacto().getEmail() : "N/A"));
        }
    }
}