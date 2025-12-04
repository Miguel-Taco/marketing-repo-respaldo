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
import java.util.HashSet;
import java.util.Set;

/**
 * Servicio para procesar:
 * 1. Webhooks de Resend (eventos de email)
 * 2. Tracking propio de clics y bajas (desde nuestros endpoints)
 * 
 * Este servicio REEMPLAZA a WebhookSendGridService.
 * 
 * Flujo de tracking:
 * - Resend envía webhooks para: delivered, opened, clicked, bounced, complained
 * - PERO para clics usamos nuestro propio tracking (/track/click) porque
 *   nos permite saber exactamente qué campaña y qué lead hizo clic
 * - Los webhooks de Resend son complementarios (para aperturas y rebotes)
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

    // Deduplicación en memoria (para eventos duplicados)
    // Resend puede enviar el mismo evento más de una vez
    private static final Set<String> eventosProcessados = new HashSet<>();
    private static final int MAX_CACHE_SIZE = 10000;

    // ========================================================================
    // TRACKING PROPIO (desde /api/v1/mailing/track/*)
    // Este es el método principal para clics y bajas
    // ========================================================================

    /**
     * Procesa un clic desde nuestro endpoint de tracking.
     * Este método se llama cuando un usuario hace clic en el botón CTA del email.
     * 
     * Flujo:
     * 1. Usuario hace clic en el CTA del email
     * 2. La URL apunta a /api/v1/mailing/track/click con parámetros
     * 3. Este método registra el clic y deriva a Ventas
     * 4. El controller redirige al usuario a la URL real (encuesta)
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

            // Verificar si ya registramos este clic (deduplicación)
            String clicKey = "clic_" + idCampana + "_" + email;
            if (eventosProcessados.contains(clicKey)) {
                log.info("  Clic ya registrado previamente, ignorando duplicado");
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

            // Marcar como procesado
            agregarACache(clicKey);

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

            // TODO: Aquí podrías marcar al lead como "no contactar por email"
            // en una futura iteración

        } catch (Exception e) {
            log.error("✗ Error procesando baja: {}", e.getMessage(), e);
        }
    }

    // ========================================================================
    // WEBHOOKS DE RESEND (desde /api/v1/mailing/webhooks/resend)
    // Estos son complementarios al tracking propio
    // ========================================================================

    /**
     * Procesa un evento webhook de Resend.
     * Útil para eventos que no podemos trackear nosotros mismos:
     * - Aperturas (Resend inserta un pixel de tracking)
     * - Rebotes (el servidor de correo rechaza)
     * - Quejas de spam
     */
    public void procesarEventoResend(ResendWebhookRequest evento) {
        if (evento == null || evento.getType() == null) {
            log.warn("Evento Resend inválido o sin tipo");
            return;
        }

        // Deduplicar por email_id + tipo
        String eventKey = evento.getEmailId() + "_" + evento.getType();
        if (eventosProcessados.contains(eventKey)) {
            log.debug("Evento {} ya procesado, ignorando", eventKey);
            return;
        }

        log.info("Webhook Resend recibido: {} para email_id: {}", 
            evento.getType(), evento.getEmailId());

        try {
            switch (evento.getType()) {
                case "email.delivered" -> procesarEntregado(evento);
                case "email.opened" -> procesarApertura(evento);
                case "email.clicked" -> procesarClicResend(evento);
                case "email.bounced" -> procesarRebote(evento);
                case "email.complained" -> procesarQueja(evento);
                case "email.sent" -> log.debug("Email enviado a Resend: {}", evento.getFirstRecipient());
                default -> log.debug("Evento no manejado: {}", evento.getType());
            }

            agregarACache(eventKey);

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
        
        // Los entregados los contamos al enviar, no aquí
        // Pero podrías usar esto para validar entregas
    }

    private void procesarApertura(ResendWebhookRequest evento) {
        String email = evento.getFirstRecipient();
        log.info("  👁 Email abierto por: {}", email);
        
        // Para aperturas necesitaríamos saber la campaña
        // Resend no nos da esa info directamente en el webhook
        // Por eso el tracking propio es mejor para clics
        
        // TODO: Podrías guardar el email_id de Resend al enviar
        // y luego hacer el match aquí
    }

    private void procesarClicResend(ResendWebhookRequest evento) {
        String email = evento.getFirstRecipient();
        String url = evento.getData().getClick() != null 
            ? evento.getData().getClick().getLink() 
            : "unknown";
        
        log.info("  🖱 Clic detectado por Resend: {} -> {}", email, url);
        
        // El clic real ya lo procesamos en procesarClicTracking()
        // Este evento de Resend es informativo/redundante
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
        
        // TODO: Marcar este email como inválido en el sistema de leads
        // Especialmente si es un "hard bounce"
    }

    private void procesarQueja(ResendWebhookRequest evento) {
        String email = evento.getFirstRecipient();
        log.warn("  🚫 Queja de SPAM de: {}", email);
        
        // TODO: Marcar este email para no enviarle más correos
        // Una queja de spam es seria y deberías respetar al usuario
    }

    // ========================================================================
    // HELPERS
    // ========================================================================

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

    private void agregarACache(String key) {
        // Limpiar cache si crece demasiado (prevenir memory leak)
        if (eventosProcessados.size() >= MAX_CACHE_SIZE) {
            log.info("Limpiando cache de eventos procesados (tamaño actual: {})", eventosProcessados.size());
            eventosProcessados.clear();
        }
        eventosProcessados.add(key);
    }
}