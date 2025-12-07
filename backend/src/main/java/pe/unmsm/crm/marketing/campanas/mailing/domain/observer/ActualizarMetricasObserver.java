package pe.unmsm.crm.marketing.campanas.mailing.domain.observer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.EventoInteraccion;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.MetricaCampana;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.JpaMetricaMailingRepository;
import pe.unmsm.crm.marketing.shared.infra.exception.NotFoundException;

import java.time.LocalDateTime;

/**
 * ✅ PATRÓN OBSERVER
 * 
 * Observer que actualiza métricas cuando ocurre una interacción.
 * 
 * RESPONSABILIDAD:
 * - Escuchar EventoInteraccion
 * - Actualizar contadores en metricas_campana
 * 
 * EVENTOS QUE MANEJA:
 * - APERTURA → Incrementa aperturas
 * - CLIC → Incrementa clics
 * - REBOTE → Incrementa rebotes
 * - BAJA → Incrementa bajas
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActualizarMetricasObserver {

    private final JpaMetricaMailingRepository metricasRepo;

    @EventListener
    @Async
    @Transactional
    public void onEventoInteraccion(EventoInteraccion evento) {
        try {
            log.debug("Observer [MÉTRICAS]: {} - Campaña {}", 
                evento.getTipoEvento().getNombre(), 
                evento.getIdCampanaMailingId());
            
            MetricaCampana metricas = metricasRepo
                    .findByCampanaMailingId(evento.getIdCampanaMailingId())
                    .orElseThrow(() -> new NotFoundException(
                        "Métricas", 
                        evento.getIdCampanaMailingId().longValue()
                    ));
            
            // Actualizar contador según tipo de evento
            switch (evento.getTipoEvento()) {
                case APERTURA -> metricas.setAperturas(metricas.getAperturas() + 1);
                case CLIC -> metricas.setClics(metricas.getClics() + 1);
                case REBOTE -> metricas.setRebotes(metricas.getRebotes() + 1);
                case BAJA -> metricas.setBajas(metricas.getBajas() + 1);
            }
            
            metricas.setActualizadoEn(LocalDateTime.now());
            metricasRepo.save(metricas);
            
            log.debug("  ✓ Métrica actualizada");
            
        } catch (Exception e) {
            log.error("Observer [MÉTRICAS]: Error - {}", e.getMessage());
        }
    }
}