package pe.unmsm.crm.marketing.campanas.mailing.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.SendGridWebhookRequest;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.*;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.IVentasPort;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.*;
import pe.unmsm.crm.marketing.shared.infra.exception.NotFoundException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WebhookSendGridService {

    private final JpaInteraccionLogRepository interaccionRepo;
    private final JpaMetricaMailingRepository metricasRepo;
    private final JpaCampanaMailingRepository campanaRepo;
    private final IVentasPort ventasPort;
    
    // Deduplicación: almacenar sendgrid_event_id ya procesados en sesión
    private static final Set<String> eventosProcessados = new HashSet<>();

    public void procesarEvento(SendGridWebhookRequest evento) {
        if (evento == null || evento.getEvent() == null) {
            log.warn("Evento SendGrid inválido o sin tipo");
            return;
        }

        // Deduplicar
        String eventId = evento.getSendgridEventId();
        if (eventId != null && eventosProcessados.contains(eventId)) {
            log.debug("Evento {} ya fue procesado, ignorando", eventId);
            return;
        }

        try {
            String tipoEvento = evento.getEvent().toLowerCase();
            
            switch (tipoEvento) {
                case "open":
                    procesarApertura(evento);
                    break;
                case "click":
                    procesarClic(evento);
                    break;
                case "bounce":
                    procesarRebote(evento);
                    break;
                case "unsubscribed":
                    procesarBaja(evento);
                    break;
                case "delivered":
                    // Opcional: actualizar estado entregado
                    log.debug("Email entregado: {}", evento.getEmail());
                    break;
                default:
                    log.warn("Tipo de evento desconocido: {}", tipoEvento);
            }
            
            // Marcar como procesado
            if (eventId != null) {
                eventosProcessados.add(eventId);
            }
            
        } catch (Exception e) {
            log.error("Error procesando evento {}: {}", evento.getEvent(), e.getMessage(), e);
        }
    }

    private void procesarApertura(SendGridWebhookRequest evento) {
        log.info("Procesando apertura: {}", evento.getEmail());
        registrarInteraccion(evento, TipoInteraccion.APERTURA, 1);
    }

    private void procesarClic(SendGridWebhookRequest evento) {
        log.info("Procesando clic: {} - URL: {}", evento.getEmail(), evento.getUrl());
        registrarInteraccion(evento, TipoInteraccion.CLIC, 2);
        
        // Derivar a Ventas
        derivarAVentas(evento);
    }

    private void procesarRebote(SendGridWebhookRequest evento) {
        log.warn("Procesando rebote: {} - Razón: {}", evento.getEmail(), evento.getReason());
        registrarInteraccion(evento, TipoInteraccion.REBOTE, 3);
    }

    private void procesarBaja(SendGridWebhookRequest evento) {
        log.warn("Procesando baja: {}", evento.getEmail());
        registrarInteraccion(evento, TipoInteraccion.BAJA, 4);
    }

    private void registrarInteraccion(SendGridWebhookRequest evento, TipoInteraccion tipo, Integer idTipo) {
        try {
            // Obtener ID de la campaña y contacto del email
            // Nota: SendGrid no envía estos IDs, necesitas guardarlos en metadata o usar email como key
            // Por MVP: buscar por email en tabla de leads
            
            Long idContactoCrm = buscarLeadPorEmail(evento.getEmail());
            if (idContactoCrm == null) {
                log.warn("No se encontró lead para email: {}", evento.getEmail());
                return;
            }
            
            // Obtener campaña (por ahora asumimos que está en contexto)
            // En producción, guardarías campaña_id en metadata de SendGrid
            
            LocalDateTime fechaEvento = evento.getTimestamp() != null 
                ? LocalDateTime.ofInstant(Instant.ofEpochSecond(evento.getTimestamp()), ZoneOffset.UTC)
                : LocalDateTime.now();
            
            // Registrar en log
            InteraccionLog log_entry = InteraccionLog.builder()
                    .idCampanaMailingId(1) // TODO: obtener de contexto
                    .idTipoEvento(idTipo)
                    .idContactoCrm(idContactoCrm)
                    .fechaEvento(fechaEvento)
                    .build();
            
            interaccionRepo.save(log_entry);
            
            // Actualizar métricas
            actualizarMetricas(1, idTipo); // TODO: obtener campaña_id
            
        } catch (Exception e) {
            log.error("Error registrando interacción: {}", e.getMessage(), e);
        }
    }

    private void actualizarMetricas(Integer idCampana, Integer idTipo) {
        try {
            var metricas = metricasRepo.findByCampanaMailingId(idCampana)
                    .orElseThrow(() -> new NotFoundException("Métricas", idCampana.longValue()));
            
            switch (idTipo) {
                case 1: // APERTURA
                    metricas.setAperturas(metricas.getAperturas() + 1);
                    break;
                case 2: // CLIC
                    metricas.setClics(metricas.getClics() + 1);
                    break;
                case 3: // REBOTE
                    metricas.setRebotes(metricas.getRebotes() + 1);
                    break;
                case 4: // BAJA
                    metricas.setBajas(metricas.getBajas() + 1);
                    break;
            }
            
            metricasRepo.save(metricas);
            log.debug("Métricas actualizadas para campaña {}", idCampana);
            
        } catch (Exception e) {
            log.error("Error actualizando métricas: {}", e.getMessage());
        }
    }

    private void derivarAVentas(SendGridWebhookRequest evento) {
        try {
            Long idLead = buscarLeadPorEmail(evento.getEmail());
            if (idLead == null) return;
            
            // TODO: obtener estos datos del contexto/metadata
            Integer idCampanaMailingId = 1;
            Integer idAgenteAsignado = 1;
            Long idSegmento = 1L;
            Long idCampanaGestion = 1L;
            
            ventasPort.derivarInteresado(idCampanaMailingId, idAgenteAsignado, idLead, idSegmento, idCampanaGestion);
            
        } catch (Exception e) {
            log.error("Error derivando a Ventas: {}", e.getMessage());
        }
    }

    private Long buscarLeadPorEmail(String email) {
        // TODO: Implementar búsqueda en BD de leads
        // SELECT lead_id FROM leads WHERE email = ?
        log.debug("Buscando lead para email: {}", email);
        return null; // Placeholder
    }
}