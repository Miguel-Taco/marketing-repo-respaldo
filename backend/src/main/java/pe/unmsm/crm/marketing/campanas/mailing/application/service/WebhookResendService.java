package pe.unmsm.crm.marketing.campanas.mailing.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.ResendWebhookRequest;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.*;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.ILeadPort;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.IVentasPort;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.*;
import pe.unmsm.crm.marketing.shared.infra.exception.NotFoundException;

import java.time.LocalDateTime;

/**
 * Servicio para procesar:
 * 1. Webhooks de Resend (eventos de email)
 * 2. Tracking propio de clics y bajas (desde nuestros endpoints)
 * 
 * CORRECCIONES APLICADAS:
 * - Eliminado Set estático para deduplicación (se pierde al reiniciar)
 * - Ahora usa BD para verificar duplicados
 * - Mejorado manejo de errores
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WebhookResendService {

    private final JpaInteraccionLogRepository interaccionRepo;
    private final JpaMetricaMailingRepository metricasRepo;
    private final JpaCampanaMailingRepository campanaRepo;
    private final IVentasPort ventasPort;
    private final ILeadPort leadPort;

    // ========================================================================
    // TRACKING PROPIO (desde /api/v1/mailing/track/*)
    // Este es el método principal para clics y bajas
    // ========================================================================

    /**
     * Procesa un clic desde nuestro endpoint de tracking.
     * Este método se llama cuando un usuario hace clic en el botón CTA del email.
     * 
     * @param idCampana ID de la campaña de mailing
     * @param email Email del destinatario que hizo clic
     */
    public void procesarClicTracking(Integer idCampana, String email) {
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║  PROCESANDO CLIC - Tracking Propio               ║");
        log.info("╠══════════════════════════════════════════════════╣");
        log.info("║  Campaña ID: {}", idCampana);
        log.info("║  Email: {}", email);
        log.info("╚══════════════════════════════════════════════════╝");
        
        try {
            // Buscar lead por email
            Long idLead = leadPort.findLeadIdByEmail(email);
            if (idLead == null) {
                log.warn("⚠ No se encontró lead para email: {}", email);
                // Aún así actualizamos métricas aunque no tengamos el lead
                actualizarMetricas(idCampana, TipoInteraccion.CLIC.getId());
                return;
            }

            // ✅ CORREGIDO: Verificar duplicado en BD (no en memoria)
            if (yaExisteInteraccion(idCampana, idLead, TipoInteraccion.CLIC.getId())) {
                log.info("  Clic ya registrado previamente en BD, ignorando duplicado");
                return;
            }

            // Registrar interacción
            InteraccionLog interaccion = InteraccionLog.builder()
                    .idCampanaMailingId(idCampana)
                    .idTipoEvento(TipoInteraccion.CLIC.getId())
                    .idContactoCrm(idLead)
                    .fechaEvento(LocalDateTime.now())
                    .build();
            interaccionRepo.save(interaccion);

            // Actualizar métricas
            actualizarMetricas(idCampana, TipoInteraccion.CLIC.getId());

            // Derivar a Ventas (el clic indica interés)
            derivarAVentas(idCampana, email, idLead);

            log.info("✓ Clic registrado exitosamente para campaña {} - lead {}", idCampana, idLead);

        } catch (Exception e) {
            log.error("✗ Error procesando clic tracking: {}", e.getMessage(), e);
        }
    }

    /**
     * Procesa una solicitud de baja (unsubscribe).
     * Se llama cuando un usuario hace clic en "Cancelar suscripción" del email.
     * 
     * @param idCampana ID de la campaña
     * @param email Email del usuario que se da de baja
     */
    public void procesarBajaTracking(Integer idCampana, String email) {
        log.warn("╔══════════════════════════════════════════════════╗");
        log.warn("║  PROCESANDO BAJA - Unsubscribe                   ║");
        log.warn("╠══════════════════════════════════════════════════╣");
        log.warn("║  Campaña ID: {}", idCampana);
        log.warn("║  Email: {}", email);
        log.warn("╚══════════════════════════════════════════════════╝");
        
        try {
            Long idLead = leadPort.findLeadIdByEmail(email);
            
            // Registrar baja aunque no encontremos el lead
            if (idLead != null) {
                // ✅ Verificar duplicado
                if (yaExisteInteraccion(idCampana, idLead, TipoInteraccion.BAJA.getId())) {
                    log.info("  Baja ya registrada previamente, ignorando");
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

            log.info("✓ Baja registrada para campaña {} - email {}", idCampana, email);

        } catch (Exception e) {
            log.error("✗ Error procesando baja: {}", e.getMessage(), e);
        }
    }

    // ========================================================================
    // WEBHOOKS DE RESEND (desde /api/v1/mailing/webhooks/resend)
    // ========================================================================

    /**
     * Procesa un evento webhook de Resend.
     */
    public void procesarEventoResend(ResendWebhookRequest evento) {
        if (evento == null || evento.getType() == null) {
            log.warn("Evento Resend inválido o sin tipo");
            return;
        }

        log.info("Webhook Resend recibido: {} para email_id: {}", 
            evento.getType(), evento.getEmailId());

        try {
            switch (evento.getType()) {
                case "email.delivered" -> procesarEntregado(evento);
                case "email.opened" -> procesarApertura(evento);
                case "email.clicked" -> log.debug("Clic detectado por Resend (ya manejado por tracking propio)");
                case "email.bounced" -> procesarRebote(evento);
                case "email.complained" -> procesarQueja(evento);
                case "email.sent" -> log.debug("Email enviado a Resend: {}", evento.getFirstRecipient());
                default -> log.debug("Evento no manejado: {}", evento.getType());
            }

        } catch (Exception e) {
            log.error("Error procesando evento Resend {}: {}", evento.getType(), e.getMessage(), e);
        }
    }

    // ========================================================================
    // PROCESADORES DE EVENTOS RESEND
    // ========================================================================

    private void procesarEntregado(ResendWebhookRequest evento) {
        String email = evento.getFirstRecipient();
        log.info("  📬 Email entregado a: {}", email);
        // Los entregados los contamos al enviar, este es informativo
    }

    private void procesarApertura(ResendWebhookRequest evento) {
        String email = evento.getFirstRecipient();
        log.info("  👁 Email abierto por: {}", email);
        // TODO: Para aperturas necesitaríamos mapear email_id -> campaign_id
        // Por ahora es solo informativo
    }

    private void procesarRebote(ResendWebhookRequest evento) {
        String email = evento.getFirstRecipient();
        String tipo = evento.getData().getBounce() != null 
            ? evento.getData().getBounce().getType() 
            : "unknown";
        String mensaje = evento.getData().getBounce() != null 
            ? evento.getData().getBounce().getMessage() 
            : "";
        
        log.warn("  ⚠ Rebote {} para: {} - {}", tipo, email, mensaje);
        // TODO: Marcar email como inválido si es hard bounce
    }

    private void procesarQueja(ResendWebhookRequest evento) {
        String email = evento.getFirstRecipient();
        log.warn("  🚫 Queja de SPAM de: {}", email);
        // TODO: Marcar este email para no enviarle más correos
    }

    // ========================================================================
    // HELPERS
    // ========================================================================

    /**
     * ✅ CORREGIDO: Verifica en BD si ya existe la interacción
     * Esto persiste entre reinicios del servidor
     */
    private boolean yaExisteInteraccion(Integer idCampana, Long idLead, Integer tipoEvento) {
        return interaccionRepo.existsByIdCampanaMailingIdAndIdContactoCrmAndIdTipoEvento(
            idCampana, idLead, tipoEvento
        );
    }

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
            
            log.debug("  Métricas actualizadas: campaña={}, tipo={}", idCampana, 
                TipoInteraccion.fromId(idTipo).getNombre());

        } catch (Exception e) {
            log.error("Error actualizando métricas: {}", e.getMessage());
        }
    }

    private void derivarAVentas(Integer idCampana, String email, Long idLead) {
        try {
            CampanaMailing campana = campanaRepo.findById(idCampana)
                    .orElseThrow(() -> new NotFoundException("CampanaMailing", idCampana.longValue()));

            log.info("  → Derivando lead {} a Ventas...", idLead);

            ventasPort.derivarInteresado(
                    campana.getId(),
                    campana.getIdAgenteAsignado(),
                    idLead,
                    campana.getIdSegmento(),
                    campana.getIdCampanaGestion()
            );

            log.info("  ✓ Lead {} derivado a Ventas desde campaña {}", idLead, idCampana);

        } catch (Exception e) {
            log.error("  ✗ Error derivando a Ventas: {}", e.getMessage());
            // No lanzar excepción - mantener resilencia
        }
    }
}