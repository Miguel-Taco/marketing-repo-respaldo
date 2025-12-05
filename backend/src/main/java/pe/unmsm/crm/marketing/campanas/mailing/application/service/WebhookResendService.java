package pe.unmsm.crm.marketing.campanas.mailing.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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
 * Servicio para procesar webhooks de Resend y tracking propio.
 * 
 * OPTIMIZACIONES APLICADAS:
 * 
 * 1. Invalidación de caché de métricas al recibir eventos
 * 2. Deduplicación en BD (no en memoria)
 * 3. Manejo resiliente de errores (no falla el flujo principal)
 * 4. Logs estructurados para debugging
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
    // ========================================================================

    /**
     * Procesa un clic desde nuestro endpoint de tracking.
     * Invalida el caché de métricas de la campaña.
     * 
     * @param idCampana ID de la campaña de mailing
     * @param email Email del destinatario que hizo clic
     */
    @CacheEvict(value = "mailing_metricas", key = "#idCampana")
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
                actualizarMetricasConEvict(idCampana, TipoInteraccion.CLIC.getId());
                return;
            }

            // Verificar duplicado en BD
            if (yaExisteInteraccion(idCampana, idLead, TipoInteraccion.CLIC.getId())) {
                log.info("  ℹ Clic ya registrado previamente, ignorando duplicado");
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
            actualizarMetricasConEvict(idCampana, TipoInteraccion.CLIC.getId());

            // Derivar a Ventas (el clic indica interés)
            derivarAVentas(idCampana, email, idLead);

            log.info("✓ Clic registrado exitosamente para campaña {} - lead {}", idCampana, idLead);

        } catch (Exception e) {
            log.error("✗ Error procesando clic tracking: {}", e.getMessage(), e);
            // No relanzar excepción para mantener resilencia
        }
    }

    /**
     * Procesa una solicitud de baja (unsubscribe).
     * Invalida el caché de métricas de la campaña.
     */
    @CacheEvict(value = "mailing_metricas", key = "#idCampana")
    public void procesarBajaTracking(Integer idCampana, String email) {
        log.warn("╔══════════════════════════════════════════════════╗");
        log.warn("║  PROCESANDO BAJA - Unsubscribe                   ║");
        log.warn("╠══════════════════════════════════════════════════╣");
        log.warn("║  Campaña ID: {}", idCampana);
        log.warn("║  Email: {}", email);
        log.warn("╚══════════════════════════════════════════════════╝");
        
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
            actualizarMetricasConEvict(idCampana, TipoInteraccion.BAJA.getId());

            log.info("✓ Baja registrada para campaña {} - email {}", idCampana, email);

        } catch (Exception e) {
            log.error("✗ Error procesando baja: {}", e.getMessage(), e);
        }
    }

    // ========================================================================
    // WEBHOOKS DE RESEND
    // ========================================================================

    /**
     * Procesa un evento webhook de Resend.
     * Los eventos de Resend NO invalidan caché directamente porque 
     * no tenemos el ID de campaña en el payload (solo email_id).
     */
    public void procesarEventoResend(ResendWebhookRequest evento) {
        if (evento == null || evento.getType() == null) {
            log.warn("Evento Resend inválido o sin tipo");
            return;
        }

        log.info("📨 Webhook Resend: {} | email_id: {}", evento.getType(), evento.getEmailId());

        try {
            switch (evento.getType()) {
                case "email.delivered" -> procesarEntregado(evento);
                case "email.opened" -> procesarApertura(evento);
                case "email.clicked" -> log.debug("Clic detectado por Resend (manejado por tracking propio)");
                case "email.bounced" -> procesarRebote(evento);
                case "email.complained" -> procesarQueja(evento);
                case "email.sent" -> log.debug("Email enviado: {}", evento.getFirstRecipient());
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
        // Informativo - los entregados se cuentan al enviar
    }

    private void procesarApertura(ResendWebhookRequest evento) {
        String email = evento.getFirstRecipient();
        log.info("  👁 Email abierto por: {}", email);
        // TODO: Implementar mapeo email_id -> campaign_id si se necesita tracking de aperturas
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
        // TODO: Marcar email para blacklist
    }

    // ========================================================================
    // MÉTODOS AUXILIARES
    // ========================================================================

    /**
     * Verifica en BD si ya existe la interacción (deduplicación persistente)
     */
    private boolean yaExisteInteraccion(Integer idCampana, Long idLead, Integer tipoEvento) {
        return interaccionRepo.existsByIdCampanaMailingIdAndIdContactoCrmAndIdTipoEvento(
            idCampana, idLead, tipoEvento
        );
    }

    /**
     * Actualiza métricas de la campaña.
     * Este método NO usa @CacheEvict porque es llamado internamente.
     * La invalidación se hace en el método público que lo llama.
     */
    private void actualizarMetricasConEvict(Integer idCampana, Integer idTipo) {
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
     * Deriva un lead interesado al módulo de Ventas.
     */
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
            // No relanzar excepción - mantener resilencia
        }
    }
}