package pe.unmsm.crm.marketing.campanas.encuestas.domain.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener de ejemplo que demuestra la extensibilidad del patrón Observer.
 * 
 * Este listener registra en logs las alertas urgentes detectadas.
 * Puede ser usado para auditoría, métricas, o debugging.
 * 
 * IMPORTANTE: Este listener se agregó SIN MODIFICAR RespuestaEncuestaService,
 * demostrando el desacoplamiento logrado con el patrón Observer.
 */
@Component
@Slf4j
public class AlertaUrgenteLoggingListener {

    /**
     * Escucha eventos de alertas urgentes y los registra en logs.
     * 
     * @param event Evento con información del lead y encuesta
     */
    @EventListener
    public void logAlertaUrgente(AlertaUrgenteDetectadaEvent event) {
        log.warn("⚠️ ALERTA URGENTE DETECTADA - Lead: {}, Encuesta: {}, Timestamp: {}",
                event.getIdLead(),
                event.getIdEncuesta(),
                event.getTimestamp());
    }
}
