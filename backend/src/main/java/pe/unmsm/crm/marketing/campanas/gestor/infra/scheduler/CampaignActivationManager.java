package pe.unmsm.crm.marketing.campanas.gestor.infra.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.input.IGestorCampanaUseCase;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@Slf4j
public class CampaignActivationManager {

    private final TaskScheduler taskScheduler;
    private final IGestorCampanaUseCase gestorCampanaUseCase;
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public CampaignActivationManager(TaskScheduler taskScheduler,
            @org.springframework.context.annotation.Lazy IGestorCampanaUseCase gestorCampanaUseCase) {
        this.taskScheduler = taskScheduler;
        this.gestorCampanaUseCase = gestorCampanaUseCase;
    }

    public void scheduleActivation(Long campanaId, LocalDateTime activationTime) {
        // Cancelar tarea previa si existe (para reprogramaciones)
        cancelActivation(campanaId);

        if (activationTime.isBefore(LocalDateTime.now())) {
            log.warn("La fecha de activación {} para la campaña {} ya pasó. Se activará inmediatamente.",
                    activationTime, campanaId);
            activarCampana(campanaId);
            return;
        }

        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> activarCampana(campanaId),
                activationTime.atZone(ZoneId.systemDefault()).toInstant());

        scheduledTasks.put(campanaId, future);
        log.info("Activación programada para campaña {} a las {}", campanaId, activationTime);
    }

    public void cancelActivation(Long campanaId) {
        ScheduledFuture<?> future = scheduledTasks.remove(campanaId);
        if (future != null) {
            future.cancel(false);
            log.info("Activación programada cancelada para campaña {}", campanaId);
        }
    }

    private void activarCampana(Long campanaId) {
        try {
            log.info(">>> EJECUTANDO ACTIVACIÓN DINÁMICA para campaña {}", campanaId);
            gestorCampanaUseCase.activar(campanaId);
            scheduledTasks.remove(campanaId); // Limpiar referencia una vez ejecutado
        } catch (Exception e) {
            log.error("Error en activación dinámica de campaña {}: {}", campanaId, e.getMessage(), e);
        }
    }
}
