package pe.unmsm.crm.marketing.campanas.gestor.infra.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.input.IGestorCampanaUseCase;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.CampanaRepositoryPort;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GestorScheduler {

    private final CampanaRepositoryPort campanaRepository;
    private final IGestorCampanaUseCase gestorCampanaUseCase;

    // Ejecuta cada 1 minuto (60000 ms)
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void activarCampanasProgramadas() {
        log.debug("=== SCHEDULER GESTOR: Buscando campañas programadas para activar ===");

        LocalDateTime ahora = LocalDateTime.now();
        List<Campana> programadas = campanaRepository.findProgramadasParaActivar(ahora);

        if (!programadas.isEmpty()) {
            log.info("Encontradas {} campañas programadas para activar", programadas.size());
        }

        for (Campana campana : programadas) {
            try {
                log.info("Activando automáticamente campaña ID: {} - {}", campana.getIdCampana(), campana.getNombre());
                gestorCampanaUseCase.activar(campana.getIdCampana());
                log.info("✓ Campaña {} activada exitosamente", campana.getIdCampana());
            } catch (Exception e) {
                log.error("✗ Error activando campaña {}: {}", campana.getIdCampana(), e.getMessage(), e);
            }
        }
    }
}
