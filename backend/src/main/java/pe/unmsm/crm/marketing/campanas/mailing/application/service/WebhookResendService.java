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
 * FLUJO PRINCIPAL DE DERIVACIÓN A VENTAS:
 * 
 * 1. Usuario recibe email de la campaña
 * 2. Usuario hace clic en el botón CTA
 * 3. La URL del CTA pasa por nuestro endpoint de tracking (/api/v1/mailing/track/click)
 * 4. Este servicio:
 *    a) Registra la interacción (clic) en la BD
 *    b) Actualiza las métricas de la campaña
 *    c) Obtiene información completa del lead
 *    d) Construye el payload para Ventas
 *    e) Envía el lead interesado a Ventas
 * 5. El usuario es redirigido a la URL real (encuesta)
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
     * 
     * ESTE ES EL MÉTODO PRINCIPAL QUE DERIVA A VENTAS.
     * 
     * @param idCampana ID de la campaña de mailing
     * @param email Email del destinatario que hizo clic
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
            // No relanzar excepción para mantener resilencia
            // El usuario será redirigido de todos modos
        }
    }

    /**
     * Procesa una solicitud de baja (unsubscribe).
     * Invalida el caché de métricas de la campaña.
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

    /**
     * Deriva un lead interesado al módulo de Ventas.
     * 
     * Este método construye el payload completo que Ventas espera y lo envía.
     * 
     * @param idCampana ID de la campaña de mailing
     * @param email Email del lead
     * @param leadInfo Información completa del lead obtenida de la BD
     */
    private void derivarLeadAVentas(Integer idCampana, String email, LeadInfoDTO leadInfo) {
        log.info("┌─────────────────────────────────────────────────────────────┐");
        log.info("│  INICIANDO DERIVACIÓN A VENTAS                              │");
        log.info("└─────────────────────────────────────────────────────────────┘");
        
        try {
            // Obtener datos de la campaña
            CampanaMailing campana = campanaRepo.findById(idCampana)
                    .orElseThrow(() -> new NotFoundException("CampanaMailing", idCampana.longValue()));

            log.info("  Campaña: {} (ID Gestión: {})", 
                campana.getNombre(), campana.getIdCampanaGestion());

            // Construir el request para Ventas
            LeadVentasRequest request = LeadVentasRequest.builder()
                    // Datos del Lead
                    .idLeadMarketing(leadInfo.getLeadId())
                    .nombres(leadInfo.getNombresParaVentas())
                    .apellidos(leadInfo.getApellidosParaVentas())
                    .correo(email)
                    .telefono(leadInfo.getTelefonoParaVentas())
                    
                    // Canal de origen (siempre CAMPANIA_MAILING para nosotros)
                    .canalOrigen("CAMPANIA_MAILING")
                    
                    // Datos de la campaña
                    .idCampaniaMarketing(campana.getIdCampanaGestion()) // ID del Gestor
                    .nombreCampania(campana.getNombre())
                    .tematica(campana.getTematica())
                    .descripcion(campana.getDescripcion())
                    
                    // Notas para el vendedor
                    .notasLlamada(LeadVentasRequest.generarNotasAutomaticas(
                            campana.getNombre(), email))
                    
                    // Fecha de envío
                    .fechaEnvio(LocalDateTime.now())
                    .build();

            // Log del request que vamos a enviar
            log.info("  Request construido:");
            log.info("    - Lead: {} {} (ID: {})", 
                request.getNombres(), request.getApellidos(), request.getIdLeadMarketing());
            log.info("    - Campaña: {} (ID: {})", 
                request.getNombreCampania(), request.getIdCampaniaMarketing());
            log.info("    - Canal: {}", request.getCanalOrigen());

            // Enviar a Ventas
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
            // No relanzar excepción - mantener resilencia
        }
    }

    // ========================================================================
    // WEBHOOKS DE RESEND (si los usas en el futuro)
    // ========================================================================

    /**
     * Procesa un evento webhook de Resend.
     * Por ahora solo registra el evento - el tracking principal se hace
     * con nuestros propios endpoints.
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
    }

    private void procesarApertura(ResendWebhookRequest evento) {
        String email = evento.getFirstRecipient();
        log.info("  👁 Email abierto por: {}", email);
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
    }

    private void procesarQueja(ResendWebhookRequest evento) {
        String email = evento.getFirstRecipient();
        log.warn("  🚫 Queja de SPAM de: {}", email);
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
}