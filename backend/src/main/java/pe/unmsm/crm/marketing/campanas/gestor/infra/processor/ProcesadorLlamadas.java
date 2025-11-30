package pe.unmsm.crm.marketing.campanas.gestor.infra.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;
import pe.unmsm.crm.marketing.campanas.telefonicas.application.CampaniaTelefonicaFacadeService;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcesadorLlamadas {

    private final CampaniaTelefonicaFacadeService facadeService;

    public void programarCampana(Campana campana) {
        log.info("Programando campaña telefónica: {}", campana.getNombre());

        facadeService.crearCampaniaTelefonicaDesdeGestor(
                campana.getIdCampana(),
                campana.getNombre(),
                campana.getIdSegmento(),
                campana.getIdEncuesta(),
                campana.getFechaProgramadaInicio().toLocalDate(),
                campana.getFechaProgramadaFin().toLocalDate(),
                campana.getIdAgente(),
                campana.getPrioridad().name());
    }

    public boolean activarCampana(Campana campana) {
        log.info("Activando campaña telefónica: {}", campana.getIdCampana());
        try {
            facadeService.activarCampania(campana.getIdCampana());
            return true;
        } catch (Exception e) {
            log.error("Error al activar campaña telefónica", e);
            return false;
        }
    }

    public void notificarPausa(Long idCampana, String motivo) {
        log.info("Pausando campaña telefónica ID: {}", idCampana);
        facadeService.pausarCampania(idCampana);
    }

    public void notificarCancelacion(Long idCampana, String motivo) {
        log.info("Cancelando campaña telefónica ID: {}", idCampana);
        facadeService.cancelarCampania(idCampana);
    }

    public void notificarReanudacion(Long idCampana) {
        log.info("Reanudando campaña telefónica ID: {}", idCampana);
        facadeService.reanudarCampania(idCampana);
    }

    public void reprogramarCampana(Campana campana) {
        log.info("Reprogramando campaña telefónica ID: {}", campana.getIdCampana());
        facadeService.actualizarCampaniaTelefonicaDesdeGestor(
                campana.getIdCampana(),
                campana.getNombre(),
                campana.getFechaProgramadaInicio().toLocalDate(),
                campana.getFechaProgramadaFin().toLocalDate(),
                campana.getPrioridad().name());
    }
}
