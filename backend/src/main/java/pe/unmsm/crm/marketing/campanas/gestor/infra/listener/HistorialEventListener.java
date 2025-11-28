package pe.unmsm.crm.marketing.campanas.gestor.infra.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.gestor.application.event.CampanaEstadoCambiadoEvent;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.HistorialCampana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.TipoAccion;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.HistorialRepositoryPort;

/**
 * Listener que escucha eventos de cambio de estado de campanas
 * y registra la auditoría en el historial.
 * 
 * Usa @Async y @Transactional(REQUIRES_NEW) para ejecutar en una transacción
 * independiente y evitar conflictos con la transacción principal.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HistorialEventListener {

    private final HistorialRepositoryPort historialRepository;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCampanaEstadoCambiado(CampanaEstadoCambiadoEvent event) {
        try {
            log.info("=== EVENTO RECIBIDO: {} para campaña {} ===", event.getTipoAccion(), event.getIdCampana());

            // Mapear el tipoAccion del evento a TipoAccion enum
            TipoAccion tipoAccion = TipoAccion.valueOf(event.getTipoAccion());

            // Construir descripción legible
            String descripcion = construirDescripcion(event);

            // Crear registro de historial
            HistorialCampana historial = HistorialCampana.builder()
                    .idCampana(event.getIdCampana())
                    .fechaAccion(event.getTimestamp())
                    .tipoAccion(tipoAccion)
                    .descripcionDetalle(descripcion)
                    .build();

            historialRepository.save(historial);

            log.info("=== HISTORIAL GUARDADO: {} para campaña {} ===", tipoAccion, event.getIdCampana());

        } catch (Exception e) {
            log.error("=== ERROR al registrar historial para campaña {}: {} ===",
                    event.getIdCampana(), e.getMessage(), e);
        }
    }

    /**
     * Construye una descripción legible del cambio de estado
     */
    private String construirDescripcion(CampanaEstadoCambiadoEvent event) {
        StringBuilder desc = new StringBuilder();
        desc.append("Cambio de estado: ")
                .append(event.getEstadoAnterior())
                .append(" → ")
                .append(event.getEstadoNuevo());

        if (event.getMotivo() != null && !event.getMotivo().isBlank()) {
            desc.append(". Motivo: ").append(event.getMotivo());
        }

        return desc.toString();
    }
}
