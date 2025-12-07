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
 * ✅ PATRÓN OBSERVER
 * 
 * Observer que registra interacciones en la base de datos.
 * 
 * RESPONSABILIDAD:
 * - Escuchar EventoInteraccion
 * - Guardar en la tabla interacciones_log
 * 
 * NOTA: Este observer se ejecuta de forma asíncrona
 * para no bloquear el hilo principal.
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
            log.debug("Observer [LOG]: {} - Campaña {}", 
                evento.getTipoEvento().getNombre(),
                evento.getIdCampanaMailingId());
            
            InteraccionLog interaccion = InteraccionLog.builder()
                    .idCampanaMailingId(evento.getIdCampanaMailingId())
                    .idTipoEvento(evento.getTipoEvento().getId())
                    .idContactoCrm(evento.getIdContactoCrm())
                    .fechaEvento(evento.getFechaEvento())
                    .build();
            
            interaccionRepo.save(interaccion);
            
            log.debug("  ✓ Interacción guardada en BD");
            
        } catch (Exception e) {
            log.error("Observer [LOG]: Error - {}", e.getMessage());
        }
    }
}