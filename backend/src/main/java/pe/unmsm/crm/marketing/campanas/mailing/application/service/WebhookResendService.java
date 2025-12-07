package pe.unmsm.crm.marketing.campanas.mailing.application.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.ResendWebhookRequest;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.response.LeadInfoDTO;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.EmailMetadata;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.EventoInteraccion;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.MetricaCampana;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.TipoInteraccion;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.ILeadPort;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.JpaEmailMetadataRepository;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.JpaInteraccionLogRepository;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.JpaMetricaMailingRepository;
import pe.unmsm.crm.marketing.shared.infra.exception.NotFoundException;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WebhookResendService {

    private final JpaInteraccionLogRepository interaccionRepo;
    private final JpaMetricaMailingRepository metricasRepo;
    private final JpaEmailMetadataRepository emailMetadataRepo;
    private final ILeadPort leadPort;
    
    //  PATRÓN OBSERVER: Inyección del Publisher
    private final EventoInteraccionPublisher eventoPublisher;

    // ========================================================================
    // WEBHOOKS DE RESEND
    // ========================================================================

    public void procesarEventoResend(ResendWebhookRequest evento) {
        if (evento == null || evento.getType() == null) {
            log.warn("Evento Resend inválido o sin tipo");
            return;
        }

        String emailId = evento.getEmailId();
        String tipoEvento = evento.getType();
        String emailDestinatario = evento.getFirstRecipient();

        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║  WEBHOOK RESEND - Patrón Observer                            ║");
        log.info("╠══════════════════════════════════════════════════════════════╣");
        log.info("║  Tipo: {}", tipoEvento);
        log.info("║  Email ID: {}", emailId);
        log.info("║  Destinatario: {}", emailDestinatario);
        log.info("╚══════════════════════════════════════════════════════════════╝");

        try {
            switch (tipoEvento) {
                case "email.delivered" -> procesarEntregado(emailId, emailDestinatario);
                case "email.opened" -> procesarApertura(emailId, emailDestinatario);
                case "email.clicked" -> log.info("  ℹ Clic (manejado por tracking propio)");
                case "email.bounced" -> procesarRebote(evento);
                case "email.complained" -> procesarQueja(emailId, emailDestinatario);
                case "email.sent" -> log.debug("  📤 Email enviado: {}", emailDestinatario);
                default -> log.debug("  ⚠ Evento no manejado: {}", tipoEvento);
            }
        } catch (Exception e) {
            log.error("  ✗ Error procesando evento: {}", e.getMessage(), e);
        }
    }

    // ========================================================================
    // PROCESAMIENTO CON PATRÓN OBSERVER
    // ========================================================================

    private void procesarEntregado(String emailId, String emailDestinatario) {
        log.info("   Procesando ENTREGA...");
        
        Optional<EmailMetadata> metadataOpt = emailMetadataRepo.findByResendEmailId(emailId);
        
        if (metadataOpt.isEmpty()) {
            log.warn("  ⚠ No se encontró metadata para email_id: {}", emailId);
            return;
        }
        
        Integer idCampana = metadataOpt.get().getIdCampanaMailing();
        actualizarMetricaEntregado(idCampana);
        
        log.info("  ✓ Entrega registrada para campaña {}", idCampana);
    }

    private void procesarApertura(String emailId, String emailDestinatario) {
        log.info("  👁 Procesando APERTURA con Observer...");
        
        Optional<EmailMetadata> metadataOpt = emailMetadataRepo.findByResendEmailId(emailId);
        
        if (metadataOpt.isEmpty()) {
            log.warn("  ⚠ No se encontró metadata");
            return;
        }
        
        EmailMetadata metadata = metadataOpt.get();
        Integer idCampana = metadata.getIdCampanaMailing();
        Long idLead = metadata.getIdLead();
        
        if (idLead == null) {
            idLead = leadPort.findLeadIdByEmail(emailDestinatario);
        }
        
        if (idLead == null) {
            log.warn("  ⚠ No se encontró lead");
            return;
        }
        
        // Verificar duplicado
        if (yaExisteInteraccion(idCampana, idLead, TipoInteraccion.APERTURA.getId())) {
            log.info("  ℹ Apertura duplicada, ignorando");
            return;
        }
        
        //  PATRÓN OBSERVER: Publicar evento
        EventoInteraccion evento = EventoInteraccion.builder()
                .idCampanaMailingId(idCampana)
                .tipoEvento(TipoInteraccion.APERTURA)
                .emailContacto(emailDestinatario)
                .idContactoCrm(idLead)
                .fechaEvento(LocalDateTime.now())
                .metadata(emailId)
                .build();
        
        log.info("   Publicando evento APERTURA...");
        eventoPublisher.publicarApertura(evento);
        log.info("  ✓ Evento publicado");
    }

    private void procesarRebote(ResendWebhookRequest evento) {
        String emailId = evento.getEmailId();
        String emailDestinatario = evento.getFirstRecipient();
        
        log.warn("  ⚠ Procesando REBOTE con Observer...");
        
        Optional<EmailMetadata> metadataOpt = emailMetadataRepo.findByResendEmailId(emailId);
        
        if (metadataOpt.isEmpty()) {
            log.warn("  ⚠ No se encontró metadata");
            return;
        }
        
        EmailMetadata metadata = metadataOpt.get();
        Integer idCampana = metadata.getIdCampanaMailing();
        Long idLead = metadata.getIdLead();
        
        if (idLead == null) {
            return;
        }
        
        if (yaExisteInteraccion(idCampana, idLead, TipoInteraccion.REBOTE.getId())) {
            log.info("  ℹ Rebote duplicado, ignorando");
            return;
        }
        
        // ✅ PATRÓN OBSERVER: Publicar evento
        EventoInteraccion eventoInt = EventoInteraccion.builder()
                .idCampanaMailingId(idCampana)
                .tipoEvento(TipoInteraccion.REBOTE)
                .emailContacto(emailDestinatario)
                .idContactoCrm(idLead)
                .fechaEvento(LocalDateTime.now())
                .metadata("rebote")
                .build();
        
        eventoPublisher.publicarRebote(eventoInt);
        log.warn("   Evento REBOTE publicado");
    }

    private void procesarQueja(String emailId, String emailDestinatario) {
        log.warn("  Procesando QUEJA con Observer...");
        
        Optional<EmailMetadata> metadataOpt = emailMetadataRepo.findByResendEmailId(emailId);
        
        if (metadataOpt.isEmpty()) {
            return;
        }
        
        EmailMetadata metadata = metadataOpt.get();
        Integer idCampana = metadata.getIdCampanaMailing();
        Long idLead = metadata.getIdLead();
        
        if (idLead == null) {
            return;
        }
        
        if (yaExisteInteraccion(idCampana, idLead, TipoInteraccion.BAJA.getId())) {
            return;
        }
        
        // ✅ PATRÓN OBSERVER: Publicar como BAJA
        EventoInteraccion evento = EventoInteraccion.builder()
                .idCampanaMailingId(idCampana)
                .tipoEvento(TipoInteraccion.BAJA)
                .emailContacto(emailDestinatario)
                .idContactoCrm(idLead)
                .fechaEvento(LocalDateTime.now())
                .metadata("spam_complaint")
                .build();
        
        eventoPublisher.publicarBaja(evento);
        log.warn("  ✓ Evento BAJA publicado");
    }

    // ========================================================================
    // TRACKING PROPIO
    // ========================================================================

    @CacheEvict(value = "mailing_metricas", key = "#idCampana")
    public void procesarClicTracking(Integer idCampana, String email) {
        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║  CLIC TRACKING - Patrón Observer                             ║");
        log.info("╠══════════════════════════════════════════════════════════════╣");
        log.info("║  Campaña: {}", idCampana);
        log.info("║  Email: {}", email);
        log.info("╚══════════════════════════════════════════════════════════════╝");
        
        try {
            Optional<LeadInfoDTO> leadInfoOpt = leadPort.findLeadInfoByEmail(email);
            
            if (leadInfoOpt.isEmpty()) {
                log.warn("  ⚠ Lead no encontrado");
                return;
            }

            LeadInfoDTO leadInfo = leadInfoOpt.get();
            Long idLead = leadInfo.getLeadId();
            
            log.info("  ✓ Lead: {} (ID: {})", leadInfo.getNombreCompleto(), idLead);

            if (yaExisteInteraccion(idCampana, idLead, TipoInteraccion.CLIC.getId())) {
                log.info("  ℹ Clic duplicado, ignorando");
                return;
            }

            // PATRÓN OBSERVER: Publicar evento CLIC
            EventoInteraccion evento = EventoInteraccion.builder()
                    .idCampanaMailingId(idCampana)
                    .tipoEvento(TipoInteraccion.CLIC)
                    .emailContacto(email)
                    .idContactoCrm(idLead)
                    .fechaEvento(LocalDateTime.now())
                    .build();
            
            log.info("  📤 Publicando evento CLIC...");
            eventoPublisher.publicarClic(evento);

            log.info("═══════════════════════════════════════════════════════════════");
            log.info("  ✓ Evento CLIC publicado - Observers procesarán");
            log.info("═══════════════════════════════════════════════════════════════");

        } catch (Exception e) {
            log.error("  ✗ Error: {}", e.getMessage(), e);
        }
    }

    @CacheEvict(value = "mailing_metricas", key = "#idCampana")
    public void procesarBajaTracking(Integer idCampana, String email) {
        log.warn("╔══════════════════════════════════════════════════════════════╗");
        log.warn("║  BAJA - Patrón Observer                                      ║");
        log.warn("╠══════════════════════════════════════════════════════════════╣");
        log.warn("║  Campaña: {}", idCampana);
        log.warn("║  Email: {}", email);
        log.warn("╚══════════════════════════════════════════════════════════════╝");
        
        try {
            Long idLead = leadPort.findLeadIdByEmail(email);
            
            if (idLead == null) {
                log.warn("  ⚠ Lead no encontrado");
                return;
            }
            
            if (yaExisteInteraccion(idCampana, idLead, TipoInteraccion.BAJA.getId())) {
                log.info("  ℹ Baja duplicada, ignorando");
                return;
            }
            
            // PATRÓN OBSERVER: Publicar evento BAJA
            EventoInteraccion evento = EventoInteraccion.builder()
                    .idCampanaMailingId(idCampana)
                    .tipoEvento(TipoInteraccion.BAJA)
                    .emailContacto(email)
                    .idContactoCrm(idLead)
                    .fechaEvento(LocalDateTime.now())
                    .metadata("unsubscribe_manual")
                    .build();
            
            eventoPublisher.publicarBaja(evento);
            log.info("  ✓ Evento BAJA publicado");

        } catch (Exception e) {
            log.error("  ✗ Error: {}", e.getMessage(), e);
        }
    }
    // MÉTODOS AUXILIARES

    private boolean yaExisteInteraccion(Integer idCampana, Long idLead, Integer tipoEvento) {
        return interaccionRepo.existsByIdCampanaMailingIdAndIdContactoCrmAndIdTipoEvento(
            idCampana, idLead, tipoEvento
        );
    }

    private void actualizarMetricaEntregado(Integer idCampana) {
        try {
            MetricaCampana metricas = metricasRepo.findByCampanaMailingId(idCampana)
                    .orElseThrow(() -> new NotFoundException("Métricas", idCampana.longValue()));

            metricas.setEntregados(metricas.getEntregados() + 1);
            metricas.setActualizadoEn(LocalDateTime.now());
            metricasRepo.save(metricas);

        } catch (Exception e) {
            log.error("Error actualizando métrica de entregados: {}", e.getMessage());
        }
    }
}