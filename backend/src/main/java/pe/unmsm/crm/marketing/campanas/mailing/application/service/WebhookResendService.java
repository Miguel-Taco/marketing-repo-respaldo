package pe.unmsm.crm.marketing.campanas.mailing.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.LeadVentasRequest;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.ResendWebhookRequest;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.response.LeadInfoDTO;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.*;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.ILeadPort;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.IVentasPort;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.*;
import pe.unmsm.crm.marketing.shared.infra.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio para procesar webhooks de Resend y tracking propio.
 * 
 * FLUJO DE WEBHOOKS DE RESEND:
 * 1. Resend envía webhook con email_id
 * 2. Buscamos en email_metadata para obtener id_campana_mailing
 * 3. Actualizamos métricas de esa campaña
 * 
 * FLUJO DE TRACKING PROPIO (CLICS):
 * 1. Usuario hace clic en CTA del correo
 * 2. URL pasa por /api/v1/mailing/track/click con cid (campaign_id)
 * 3. Registramos interacción y derivamos a Ventas
 * 4. Redirigimos a la URL real (encuesta)
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WebhookResendService {

    private final JpaInteraccionLogRepository interaccionRepo;
    private final JpaMetricaMailingRepository metricasRepo;
    private final JpaCampanaMailingRepository campanaRepo;
    private final JpaEmailMetadataRepository emailMetadataRepo;
    private final IVentasPort ventasPort;
    private final ILeadPort leadPort;

    // ========================================================================
    // WEBHOOKS DE RESEND (email.opened, email.delivered, etc.)
    // ========================================================================

    /**
     * Procesa un evento webhook de Resend.
     * 
     * IMPORTANTE: Usamos el email_id del webhook para buscar en email_metadata
     * y así obtener el id_campana_mailing correspondiente.
     */
    public void procesarEventoResend(ResendWebhookRequest evento) {
        if (evento == null || evento.getType() == null) {
            log.warn("Evento Resend inválido o sin tipo");
            return;
        }

        String emailId = evento.getEmailId();
        String tipoEvento = evento.getType();
        String emailDestinatario = evento.getFirstRecipient();

        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║  PROCESANDO WEBHOOK RESEND                                   ║");
        log.info("╠══════════════════════════════════════════════════════════════╣");
        log.info("║  Tipo: {}", tipoEvento);
        log.info("║  Email ID: {}", emailId);
        log.info("║  Destinatario: {}", emailDestinatario);
        log.info("╚══════════════════════════════════════════════════════════════╝");

        try {
            switch (tipoEvento) {
                case "email.delivered" -> procesarEntregado(emailId, emailDestinatario);
                case "email.opened" -> procesarApertura(emailId, emailDestinatario);
                case "email.clicked" -> log.info("  ℹ Clic detectado por Resend (manejado por tracking propio)");
                case "email.bounced" -> procesarRebote(evento);
                case "email.complained" -> procesarQueja(emailId, emailDestinatario);
                case "email.sent" -> log.debug("  📤 Email enviado: {}", emailDestinatario);
                default -> log.debug("  ⚠ Evento no manejado: {}", tipoEvento);
            }

        } catch (Exception e) {
            log.error("  ✗ Error procesando evento Resend {}: {}", tipoEvento, e.getMessage(), e);
        }
    }

    /**
     * Procesa evento de entrega (email.delivered)
     */
    private void procesarEntregado(String emailId, String emailDestinatario) {
        log.info("  📬 Procesando ENTREGA...");
        
        // Buscar metadata por email_id de Resend
        Optional<EmailMetadata> metadataOpt = emailMetadataRepo.findByResendEmailId(emailId);
        
        if (metadataOpt.isEmpty()) {
            log.warn("  ⚠ No se encontró metadata para email_id: {}", emailId);
            log.warn("    Intentando buscar por email destinatario...");
            
            // Fallback: buscar por email si no hay email_id (para emails enviados antes de la corrección)
            // Esto no es ideal porque un email puede estar en múltiples campañas
            return;
        }
        
        EmailMetadata metadata = metadataOpt.get();
        Integer idCampana = metadata.getIdCampanaMailing();
        
        log.info("  ✓ Campaña identificada: {}", idCampana);
        
        // Actualizar métrica de entregados
        actualizarMetricaEntregado(idCampana);
        
        log.info("  ✓ Entrega registrada para campaña {}", idCampana);
    }

    /**
     * ✅ CORREGIDO: Procesa evento de apertura (email.opened)
     * Ahora busca la campaña usando email_metadata y actualiza métricas
     */
    private void procesarApertura(String emailId, String emailDestinatario) {
        log.info("  👁 Procesando APERTURA...");
        
        // Buscar metadata por email_id de Resend
        Optional<EmailMetadata> metadataOpt = emailMetadataRepo.findByResendEmailId(emailId);
        
        if (metadataOpt.isEmpty()) {
            log.warn("  ⚠ No se encontró metadata para email_id: {}", emailId);
            return;
        }
        
        EmailMetadata metadata = metadataOpt.get();
        Integer idCampana = metadata.getIdCampanaMailing();
        Long idLead = metadata.getIdLead();
        
        log.info("  ✓ Campaña identificada: {}", idCampana);
        log.info("  ✓ Lead ID: {}", idLead);
        
        // Verificar duplicado (evitar contar múltiples aperturas del mismo usuario)
        if (idLead != null && yaExisteInteraccion(idCampana, idLead, TipoInteraccion.APERTURA.getId())) {
            log.info("  ℹ Apertura ya registrada para este lead, ignorando duplicado");
            return;
        }
        
        // Si no tenemos lead_id, intentar buscarlo
        if (idLead == null) {
            idLead = leadPort.findLeadIdByEmail(emailDestinatario);
        }
        
        // Registrar interacción si tenemos el lead
        if (idLead != null) {
            InteraccionLog interaccion = InteraccionLog.builder()
                    .idCampanaMailingId(idCampana)
                    .idTipoEvento(TipoInteraccion.APERTURA.getId())
                    .idContactoCrm(idLead)
                    .fechaEvento(LocalDateTime.now())
                    .build();
            interaccionRepo.save(interaccion);
            log.info("  ✓ Interacción de apertura registrada");
        }
        
        // ✅ Actualizar métrica de aperturas
        actualizarMetricas(idCampana, TipoInteraccion.APERTURA.getId());
        
        log.info("  ✓ Apertura registrada para campaña {} - email {}", idCampana, emailDestinatario);
    }

    /**
     * Procesa evento de rebote (email.bounced)
     */
    private void procesarRebote(ResendWebhookRequest evento) {
        String emailId = evento.getEmailId();
        String emailDestinatario = evento.getFirstRecipient();
        String tipoRebote = evento.getData().getBounce() != null 
            ? evento.getData().getBounce().getType() 
            : "unknown";
        String mensaje = evento.getData().getBounce() != null 
            ? evento.getData().getBounce().getMessage() 
            : "";
        
        log.warn("  ⚠ Procesando REBOTE ({})...", tipoRebote);
        log.warn("    Mensaje: {}", mensaje);
        
        Optional<EmailMetadata> metadataOpt = emailMetadataRepo.findByResendEmailId(emailId);
        
        if (metadataOpt.isEmpty()) {
            log.warn("  ⚠ No se encontró metadata para email_id: {}", emailId);
            return;
        }
        
        EmailMetadata metadata = metadataOpt.get();
        Integer idCampana = metadata.getIdCampanaMailing();
        Long idLead = metadata.getIdLead();
        
        // Registrar interacción
        if (idLead != null) {
            InteraccionLog interaccion = InteraccionLog.builder()
                    .idCampanaMailingId(idCampana)
                    .idTipoEvento(TipoInteraccion.REBOTE.getId())
                    .idContactoCrm(idLead)
                    .fechaEvento(LocalDateTime.now())
                    .build();
            interaccionRepo.save(interaccion);
        }
        
        // Actualizar métrica de rebotes
        actualizarMetricas(idCampana, TipoInteraccion.REBOTE.getId());
        
        log.warn("  ✓ Rebote registrado para campaña {} - email {}", idCampana, emailDestinatario);
    }

    /**
     * Procesa evento de queja/spam (email.complained)
     */
    private void procesarQueja(String emailId, String emailDestinatario) {
        log.warn("  🚫 Procesando QUEJA (spam)...");
        
        Optional<EmailMetadata> metadataOpt = emailMetadataRepo.findByResendEmailId(emailId);
        
        if (metadataOpt.isEmpty()) {
            log.warn("  ⚠ No se encontró metadata para email_id: {}", emailId);
            return;
        }
        
        EmailMetadata metadata = metadataOpt.get();
        Integer idCampana = metadata.getIdCampanaMailing();
        Long idLead = metadata.getIdLead();
        
        // Registrar como baja
        if (idLead != null) {
            InteraccionLog interaccion = InteraccionLog.builder()
                    .idCampanaMailingId(idCampana)
                    .idTipoEvento(TipoInteraccion.BAJA.getId())
                    .idContactoCrm(idLead)
                    .fechaEvento(LocalDateTime.now())
                    .build();
            interaccionRepo.save(interaccion);
        }
        
        // Actualizar métrica de bajas
        actualizarMetricas(idCampana, TipoInteraccion.BAJA.getId());
        
        log.warn("  ✓ Queja registrada como baja para campaña {} - email {}", idCampana, emailDestinatario);
    }

    // ========================================================================
    // TRACKING PROPIO (desde /api/v1/mailing/track/*)
    // ========================================================================

    /**
     * Procesa un clic desde nuestro endpoint de tracking.
     * 
     * ESTE ES EL MÉTODO PRINCIPAL QUE DERIVA A VENTAS.
     */
    @CacheEvict(value = "mailing_metricas", key = "#idCampana")
    public void procesarClicTracking(Integer idCampana, String email) {
        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║          PROCESANDO CLIC - Tracking Propio                   ║");
        log.info("╠══════════════════════════════════════════════════════════════╣");
        log.info("║  Campaña ID: {}", idCampana);
        log.info("║  Email: {}", email);
        log.info("╚══════════════════════════════════════════════════════════════╝");
        
        try {
            // 1. Obtener información completa del lead
            Optional<LeadInfoDTO> leadInfoOpt = leadPort.findLeadInfoByEmail(email);
            
            if (leadInfoOpt.isEmpty()) {
                log.warn("  ⚠ No se encontró lead para email: {}", email);
                // Aún así actualizamos métricas aunque no tengamos el lead
                actualizarMetricas(idCampana, TipoInteraccion.CLIC.getId());
                return;
            }

            LeadInfoDTO leadInfo = leadInfoOpt.get();
            Long idLead = leadInfo.getLeadId();
            
            log.info("  ✓ Lead encontrado: ID={}, Nombre={}", 
                idLead, leadInfo.getNombreCompleto());

            // 2. Verificar duplicado en BD
            if (yaExisteInteraccion(idCampana, idLead, TipoInteraccion.CLIC.getId())) {
                log.info("  ℹ Clic ya registrado previamente para este lead, ignorando duplicado");
                return;
            }

            // 3. Registrar interacción
            InteraccionLog interaccion = InteraccionLog.builder()
                    .idCampanaMailingId(idCampana)
                    .idTipoEvento(TipoInteraccion.CLIC.getId())
                    .idContactoCrm(idLead)
                    .fechaEvento(LocalDateTime.now())
                    .build();
            interaccionRepo.save(interaccion);
            log.info("  ✓ Interacción de clic registrada");

            // 4. Actualizar métricas
            actualizarMetricas(idCampana, TipoInteraccion.CLIC.getId());
            log.info("  ✓ Métricas actualizadas");

            // 5. DERIVAR A VENTAS (la parte más importante)
            derivarLeadAVentas(idCampana, email, leadInfo);

            log.info("═══════════════════════════════════════════════════════════════");
            log.info("  PROCESAMIENTO DE CLIC COMPLETADO EXITOSAMENTE");
            log.info("═══════════════════════════════════════════════════════════════");

        } catch (Exception e) {
            log.error("  ✗ Error procesando clic tracking: {}", e.getMessage(), e);
        }
    }

    /**
     * Procesa una solicitud de baja (unsubscribe).
     */
    @CacheEvict(value = "mailing_metricas", key = "#idCampana")
    public void procesarBajaTracking(Integer idCampana, String email) {
        log.warn("╔══════════════════════════════════════════════════════════════╗");
        log.warn("║          PROCESANDO BAJA - Unsubscribe                       ║");
        log.warn("╠══════════════════════════════════════════════════════════════╣");
        log.warn("║  Campaña ID: {}", idCampana);
        log.warn("║  Email: {}", email);
        log.warn("╚══════════════════════════════════════════════════════════════╝");
        
        try {
            Long idLead = leadPort.findLeadIdByEmail(email);
            
            if (idLead != null) {
                // Verificar duplicado
                if (yaExisteInteraccion(idCampana, idLead, TipoInteraccion.BAJA.getId())) {
                    log.info("  ℹ Baja ya registrada previamente, ignorando");
                    return;
                }
                
                InteraccionLog interaccion = InteraccionLog.builder()
                        .idCampanaMailingId(idCampana)
                        .idTipoEvento(TipoInteraccion.BAJA.getId())
                        .idContactoCrm(idLead)
                        .fechaEvento(LocalDateTime.now())
                        .build();
                interaccionRepo.save(interaccion);
            }

            // Actualizar métricas
            actualizarMetricas(idCampana, TipoInteraccion.BAJA.getId());

            log.info("  ✓ Baja registrada para campaña {} - email {}", idCampana, email);

        } catch (Exception e) {
            log.error("  ✗ Error procesando baja: {}", e.getMessage(), e);
        }
    }

    // ========================================================================
    // DERIVACIÓN A VENTAS
    // ========================================================================

    private void derivarLeadAVentas(Integer idCampana, String email, LeadInfoDTO leadInfo) {
        log.info("┌─────────────────────────────────────────────────────────────┐");
        log.info("│  INICIANDO DERIVACIÓN A VENTAS                              │");
        log.info("└─────────────────────────────────────────────────────────────┘");
        
        try {
            CampanaMailing campana = campanaRepo.findById(idCampana)
                    .orElseThrow(() -> new NotFoundException("CampanaMailing", idCampana.longValue()));

            log.info("  Campaña: {} (ID Gestión: {})", 
                campana.getNombre(), campana.getIdCampanaGestion());

            LeadVentasRequest request = LeadVentasRequest.builder()
                    .idLeadMarketing(leadInfo.getLeadId())
                    .nombres(leadInfo.getNombresParaVentas())
                    .apellidos(leadInfo.getApellidosParaVentas())
                    .correo(email)
                    .telefono(leadInfo.getTelefonoParaVentas())
                    .canalOrigen("CAMPANIA_MAILING")
                    .idCampaniaMarketing(campana.getIdCampanaGestion())
                    .nombreCampania(campana.getNombre())
                    .tematica(campana.getTematica())
                    .descripcion(campana.getDescripcion())
                    .notasLlamada(LeadVentasRequest.generarNotasAutomaticas(
                            campana.getNombre(), email))
                    .fechaEnvio(LocalDateTime.now())
                    .build();

            log.info("  Request construido:");
            log.info("    - Lead: {} {} (ID: {})", 
                request.getNombres(), request.getApellidos(), request.getIdLeadMarketing());
            log.info("    - Campaña: {} (ID: {})", 
                request.getNombreCampania(), request.getIdCampaniaMarketing());
            log.info("    - Canal: {}", request.getCanalOrigen());

            boolean exito = ventasPort.derivarLeadInteresado(request);

            if (exito) {
                log.info("  ✓ Lead derivado exitosamente a Ventas");
            } else {
                log.warn("  ⚠ No se pudo derivar el lead a Ventas (ver logs anteriores)");
            }

        } catch (NotFoundException e) {
            log.error("  ✗ Campaña no encontrada: {}", idCampana);
        } catch (Exception e) {
            log.error("  ✗ Error derivando a Ventas: {}", e.getMessage(), e);
        }
    }

    // ========================================================================
    // MÉTODOS AUXILIARES
    // ========================================================================

    private boolean yaExisteInteraccion(Integer idCampana, Long idLead, Integer tipoEvento) {
        return interaccionRepo.existsByIdCampanaMailingIdAndIdContactoCrmAndIdTipoEvento(
            idCampana, idLead, tipoEvento
        );
    }

    /**
     * Actualiza métricas de la campaña según el tipo de evento.
     */
    private void actualizarMetricas(Integer idCampana, Integer idTipo) {
        try {
            MetricaCampana metricas = metricasRepo.findByCampanaMailingId(idCampana)
                    .orElseThrow(() -> new NotFoundException("Métricas", idCampana.longValue()));

            switch (idTipo) {
                case 1 -> metricas.setAperturas(metricas.getAperturas() + 1);  // APERTURA
                case 2 -> metricas.setClics(metricas.getClics() + 1);          // CLIC
                case 3 -> metricas.setRebotes(metricas.getRebotes() + 1);      // REBOTE
                case 4 -> metricas.setBajas(metricas.getBajas() + 1);          // BAJA
            }

            metricas.setActualizadoEn(LocalDateTime.now());
            metricasRepo.save(metricas);
            
            log.debug("  📊 Métricas actualizadas: campaña={}, tipo={}", 
                idCampana, TipoInteraccion.fromId(idTipo).getNombre());

        } catch (Exception e) {
            log.error("Error actualizando métricas: {}", e.getMessage());
        }
    }

    /**
     * Actualiza solo la métrica de entregados.
     */
    private void actualizarMetricaEntregado(Integer idCampana) {
        try {
            MetricaCampana metricas = metricasRepo.findByCampanaMailingId(idCampana)
                    .orElseThrow(() -> new NotFoundException("Métricas", idCampana.longValue()));

            metricas.setEntregados(metricas.getEntregados() + 1);
            metricas.setActualizadoEn(LocalDateTime.now());
            metricasRepo.save(metricas);
            
            log.debug("  📊 Métrica de entregados actualizada: campaña={}", idCampana);

        } catch (Exception e) {
            log.error("Error actualizando métrica de entregados: {}", e.getMessage());
        }
    }
}