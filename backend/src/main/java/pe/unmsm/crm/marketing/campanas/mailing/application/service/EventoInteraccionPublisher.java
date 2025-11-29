package pe.unmsm.crm.marketing.campanas.mailing.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.EventoInteraccion;

/**
 * Publisher de eventos de interacción
 * Permite notificar a observers cuando ocurren eventos
 * (apertura, clic, rebote, baja)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventoInteraccionPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publica un evento de interacción
     * Los observers suscritos (ActualizarMetricasObserver, DerivarAVentasObserver, etc)
     * serán notificados automáticamente
     */
    public void publicarEvento(EventoInteraccion evento) {
        log.debug("Publicando evento: {} - Email: {}", 
            evento.getTipoEvento(), evento.getEmailContacto());
        
        try {
            eventPublisher.publishEvent(evento);
            log.debug("✓ Evento publicado correctamente");
        } catch (Exception e) {
            log.error("Error publicando evento: {}", e.getMessage(), e);
            // No lanzar excepción para no afectar el flujo principal
        }
    }

    /**
     * Publica evento de apertura
     */
    public void publicarApertura(EventoInteraccion evento) {
        log.info("Publicando evento APERTURA para campaña {}", evento.getIdCampanaMailingId());
        publicarEvento(evento);
    }

    /**
     * Publica evento de clic
     */
    public void publicarClic(EventoInteraccion evento) {
        log.info("Publicando evento CLIC para campaña {}", evento.getIdCampanaMailingId());
        publicarEvento(evento);
    }

    /**
     * Publica evento de rebote
     */
    public void publicarRebote(EventoInteraccion evento) {
        log.warn("Publicando evento REBOTE para campaña {}", evento.getIdCampanaMailingId());
        publicarEvento(evento);
    }

    /**
     * Publica evento de baja
     */
    public void publicarBaja(EventoInteraccion evento) {
        log.warn("Publicando evento BAJA para campaña {}", evento.getIdCampanaMailingId());
        publicarEvento(evento);
    }
}