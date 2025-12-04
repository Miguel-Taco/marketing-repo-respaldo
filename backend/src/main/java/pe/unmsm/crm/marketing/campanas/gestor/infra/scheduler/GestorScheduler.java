package pe.unmsm.crm.marketing.campanas.gestor.infra.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.CampanaRepositoryPort;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@org.springframework.context.annotation.Profile("!console")
public class GestorScheduler implements ApplicationRunner {

    private final CampanaRepositoryPort campanaRepository;
    private final CampaignActivationManager activationManager;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== GESTOR SCHEDULER: Iniciando carga de campañas programadas ===");

        List<Campana> programadas = campanaRepository.findProgramadasPendientes();

        if (programadas.isEmpty()) {
            log.info("No se encontraron campañas programadas pendientes.");
            return;
        }

        log.info("Encontradas {} campañas programadas. Agendando activación...", programadas.size());

        for (Campana campana : programadas) {
            try {
                if (campana.getFechaProgramadaInicio() != null) {
                    activationManager.scheduleActivation(campana.getIdCampana(), campana.getFechaProgramadaInicio());
                } else {
                    log.warn("Campaña {} está programada pero no tiene fecha de inicio.", campana.getIdCampana());
                }
            } catch (Exception e) {
                log.error("Error al agendar activación para campaña {}: {}", campana.getIdCampana(), e.getMessage(), e);
            }
        }

        log.info("=== GESTOR SCHEDULER: Carga inicial completada ===");
    }
}
