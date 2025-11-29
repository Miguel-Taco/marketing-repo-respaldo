package pe.unmsm.crm.marketing.campanas.mailing.domain.observer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.EventoInteraccion;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.InteraccionLog;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.JpaInteraccionLogRepository;

/**
 * Observer que registra las interacciones en la base de datos
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RegistrarInteraccionObserver {

    private final JpaInteraccionLogRepository interaccionRepo;

    @EventListener
    @Async
    @Transactional
    public void onEventoInteraccion(EventoInteraccion evento) {
        try {
            log.debug("Observer: Registrando interacción {} para campaña {}", 
                evento.getTipoEvento(), evento.getIdCampanaMailingId());
            
            InteraccionLog log = InteraccionLog.builder()
                    .idCampanaMailingId(evento.getIdCampanaMailingId())
                    .idTipoEvento(evento.getTipoEvento().getId())
                    .idContactoCrm(evento.getIdContactoCrm())
                    .fechaEvento(evento.getFechaEvento())
                    .build();
            
            interaccionRepo.save(log);
            
            log.debug("✓ Interacción registrada en BD");
            
        } catch (Exception e) {
            log.error("Error registrando interacción: {}", e.getMessage(), e);
        }
    }
}