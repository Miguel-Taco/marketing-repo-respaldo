package pe.unmsm.crm.marketing.campanas.mailing.domain.observer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.CampanaMailing;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.EventoInteraccion;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.TipoInteraccion;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.IVentasPort;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.JpaCampanaMailingRepository;

/**
 * Observer que deriva leads interesados a Ventas
 * Solo se activa cuando hay evento de CLIC (interés activo)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DerivarAVentasObserver {

    private final IVentasPort ventasPort;
    private final JpaCampanaMailingRepository campanaRepo;

    @EventListener
    @Async
    public void onEventoInteraccion(EventoInteraccion evento) {
        try {
            // Solo derivar cuando hay CLIC (indica interés real)
            if (evento.getTipoEvento() != TipoInteraccion.CLIC) {
                return;
            }
            
            log.info("Observer: Derivando lead {} a Ventas (campaña {})", 
                evento.getIdContactoCrm(), evento.getIdCampanaMailingId());
            
            // Obtener datos de la campaña
            CampanaMailing campana = campanaRepo.findById(evento.getIdCampanaMailingId())
                    .orElse(null);
            
            if (campana == null) {
                log.warn("No se encontró campaña {} para derivación", evento.getIdCampanaMailingId());
                return;
            }
            
            // Derivar a Ventas
            ventasPort.derivarInteresado(
                    campana.getId(),
                    campana.getIdAgenteAsignado(),
                    evento.getIdContactoCrm(),
                    campana.getIdSegmento(),
                    campana.getIdCampanaGestion()
            );
            
            log.info("✓ Lead {} derivado a Ventas", evento.getIdContactoCrm());
            
        } catch (Exception e) {
            log.error("Error derivando a Ventas: {}", e.getMessage(), e);
            // No lanzar excepción para no afectar otros observers
        }
    }
}