package pe.unmsm.crm.marketing.campanas.gestor.infra.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.CanalEjecucion;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.ICanalEjecucionPort;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.CampanaRepositoryPort;
import pe.unmsm.crm.marketing.shared.infra.exception.BusinessException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CanalEjecucionRouter implements ICanalEjecucionPort {

    private final ProcesadorMailing procesadorMailing;
    private final ProcesadorLlamadas procesadorLlamadas;
    private final CampanaRepositoryPort campanaRepository;

    @Override
    public void programarCampana(Campana campana) {
        log.info("Enrutando programación de campaña {} al canal {}", campana.getIdCampana(),
                campana.getCanalEjecucion());
        if (campana.getCanalEjecucion() == CanalEjecucion.Mailing) {
            procesadorMailing.programarCampana(campana);
        } else if (campana.getCanalEjecucion() == CanalEjecucion.Llamadas) {
            procesadorLlamadas.programarCampana(campana);
        }
    }

    @Override
    public boolean activarCampana(Campana campana) {
        log.info("Enrutando activación de campaña {} al canal {}", campana.getIdCampana(), campana.getCanalEjecucion());
        if (campana.getCanalEjecucion() == CanalEjecucion.Mailing) {
            return procesadorMailing.activarCampana(campana);
        } else if (campana.getCanalEjecucion() == CanalEjecucion.Llamadas) {
            return procesadorLlamadas.activarCampana(campana);
        }
        return false;
    }

    @Override
    public void notificarPausa(Long idCampana, String motivo) {
        Campana campana = obtenerCampana(idCampana);
        if (campana.getCanalEjecucion() == CanalEjecucion.Mailing) {
            procesadorMailing.notificarPausa(idCampana, motivo);
        } else if (campana.getCanalEjecucion() == CanalEjecucion.Llamadas) {
            procesadorLlamadas.notificarPausa(idCampana, motivo);
        }
    }

    @Override
    public void notificarCancelacion(Long idCampana, String motivo) {
        Campana campana = obtenerCampana(idCampana);
        if (campana.getCanalEjecucion() == CanalEjecucion.Mailing) {
            procesadorMailing.notificarCancelacion(idCampana, motivo);
        } else if (campana.getCanalEjecucion() == CanalEjecucion.Llamadas) {
            procesadorLlamadas.notificarCancelacion(idCampana, motivo);
        }
    }

    @Override
    public void notificarReanudacion(Long idCampana) {
        Campana campana = obtenerCampana(idCampana);
        if (campana.getCanalEjecucion() == CanalEjecucion.Mailing) {
            procesadorMailing.notificarReanudacion(idCampana);
        } else if (campana.getCanalEjecucion() == CanalEjecucion.Llamadas) {
            procesadorLlamadas.notificarReanudacion(idCampana);
        }
    }

    @Override
    public void reprogramarCampana(Campana campana) {
        if (campana.getCanalEjecucion() == CanalEjecucion.Mailing) {
            procesadorMailing.reprogramarCampana(campana);
        } else if (campana.getCanalEjecucion() == CanalEjecucion.Llamadas) {
            procesadorLlamadas.reprogramarCampana(campana);
        }
    }

    private Campana obtenerCampana(Long idCampana) {
        return campanaRepository.findById(idCampana)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Campaña no encontrada para enrutamiento"));
    }
}
