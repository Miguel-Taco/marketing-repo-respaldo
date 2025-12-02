package pe.unmsm.crm.marketing.campanas.telefonicas.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.gestor.application.event.CampanaEstadoCambiadoEvent;
import pe.unmsm.crm.marketing.campanas.telefonicas.application.CampaniaTelefonicaFacadeService;

/**
 * Event listener que escucha eventos del Gestor de Campañas y actualiza
 * las campañas telefónicas en consecuencia.
 * 
 * Esto permite que el módulo de campañas telefónicas reaccione a cambios
 * de estado en el gestor sin necesidad de modificar código del gestor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CampaniaGestorEventListener {

    private final CampaniaTelefonicaFacadeService facadeService;

    /**
     * Escucha eventos de cambio de estado de campaña desde el Gestor.
     * Actualmente maneja el evento de FINALIZACION.
     */
    @EventListener
    @Transactional
    public void onCampanaEstadoCambiado(CampanaEstadoCambiadoEvent event) {
        log.debug("Evento recibido: {} para campaña {}", event.getTipoAccion(), event.getIdCampana());

        // Solo procesar eventos de FINALIZACION
        // Los demás eventos (PROGRAMACION, PAUSA, etc.) ya son manejados por
        // ProcesadorLlamadas
        if ("FINALIZACION".equals(event.getTipoAccion())) {
            log.info("Procesando finalización de campaña telefónica desde gestor: {}", event.getIdCampana());
            try {
                facadeService.finalizarCampania(event.getIdCampana());
            } catch (Exception e) {
                log.error("Error al finalizar campaña telefónica {}: {}",
                        event.getIdCampana(), e.getMessage(), e);
            }
        }
    }
}
