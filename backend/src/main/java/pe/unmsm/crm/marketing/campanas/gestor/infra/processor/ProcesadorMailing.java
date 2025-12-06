package pe.unmsm.crm.marketing.campanas.gestor.infra.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.CrearCampanaMailingRequest;
import pe.unmsm.crm.marketing.campanas.mailing.application.service.CampanaMailingService;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.CampanaMailing;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcesadorMailing {

    private final CampanaMailingService campanaMailingService;

    @Value("${app.encuestas_frontend.url}")
    private String encuestasFrontendUrl;

    public void programarCampana(Campana campana) {
        log.info("Programando campaña de mailing: {}", campana.getNombre());

        CrearCampanaMailingRequest request = CrearCampanaMailingRequest.builder()
                .idCampanaGestion(campana.getIdCampana())
                .nombre(campana.getNombre())
                .tematica(campana.getTematica())
                .descripcion(campana.getDescripcion())
                .prioridad(campana.getPrioridad() != null ? campana.getPrioridad().name() : "Media")
                .fechaInicio(campana.getFechaProgramadaInicio())
                .fechaFin(campana.getFechaProgramadaFin())
                .idSegmento(campana.getIdSegmento())
                .idEncuesta(campana.getIdEncuesta())
                .idAgenteAsignado(campana.getIdAgente())
                .ctaUrl(null)
                .build();

        campanaMailingService.crearCampana(request);
    }

    public boolean activarCampana(Campana campana) {
        log.info("Activando campaña de mailing: {}", campana.getIdCampana());
        try {
            // Buscar la campaña de mailing asociada (asumimos que existe por
            // idCampanaGestion)
            // Nota: CampanaMailingService no tiene método directo por idCampanaGestion,
            // pero podemos buscarla o asumir que el ID es consistente si se sincronizara.
            // Por ahora, buscaremos en la lista de pendientes del agente.
            // TODO: Agregar método findByIdCampanaGestion en CampanaMailingService para
            // mayor precisión.

            // Solución temporal: Listar todas y filtrar (ineficiente pero funcional para
            // MVP)
            CampanaMailing mailing = campanaMailingService
                    .listarTodas(java.util.Collections.singletonList(campana.getIdAgente())).stream()
                    .filter(c -> c.getIdCampanaGestion().equals(campana.getIdCampana()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "Campaña mailing no encontrada para gestión ID: " + campana.getIdCampana()));

            campanaMailingService.marcarListo(mailing.getId());
            return true;
        } catch (Exception e) {
            log.error("Error al activar campaña mailing", e);
            return false;
        }
    }

    public void notificarPausa(Long idCampana, String motivo) {
        log.info("Pausando campaña mailing ID Gestión: {}", idCampana);
        campanaMailingService.pausarPorGestor(idCampana);
    }

    public void notificarCancelacion(Long idCampana, String motivo) {
        log.info("Cancelando campaña mailing ID Gestión: {}", idCampana);
        campanaMailingService.cancelarPorGestor(idCampana);
    }

    public void notificarReanudacion(Long idCampana) {
        log.info("Reanudando campaña mailing ID Gestión: {}", idCampana);
        // Se espera implementación futura de API endpoint en Mailing
    }

    public void reprogramarCampana(Campana campana) {
        log.info("Reprogramando campaña mailing ID Gestión: {}", campana.getIdCampana());

        pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.ReprogramarCampanaRequest req = pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.ReprogramarCampanaRequest
                .builder()
                .fechaInicio(campana.getFechaProgramadaInicio())
                .fechaFin(campana.getFechaProgramadaFin())
                .build();

        campanaMailingService.reprogramarPorGestor(campana.getIdCampana(), req);
    }
}
