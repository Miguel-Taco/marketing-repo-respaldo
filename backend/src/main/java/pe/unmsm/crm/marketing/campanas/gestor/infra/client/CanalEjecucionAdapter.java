package pe.unmsm.crm.marketing.campanas.gestor.infra.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.ICanalEjecucionPort;

/**
 * Adaptador HTTP para delegar la ejecución de campanas
 * a los módulos de Mailing o Llamadas.
 * 
 * PLACEHOLDER: Los módulos de destino aún no están implementados.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CanalEjecucionAdapter implements ICanalEjecucionPort {

    @Override
    public boolean ejecutarCampana(Campana campana) {
        log.warn("PLACEHOLDER: ejecutarCampana() - Módulo de {} no implementado aún. CampanaID: {}",
                campana.getCanalEjecucion(), campana.getIdCampana());
        // TODO: Implementar cuando existan APIs de Mailing/Llamadas
        // POST /api/v1/mailing/ejecutar o POST /api/v1/llamadas/ejecutar
        return true; // Simula éxito
    }

    @Override
    public void notificarPausa(Long idCampana, String motivo) {
        log.warn("PLACEHOLDER: notificarPausa() - CampanaID: {}, Motivo: {}", idCampana, motivo);
        // TODO: POST /api/v1/{canal}/pausar/{idCampana}
    }

    @Override
    public void notificarCancelacion(Long idCampana, String motivo) {
        log.warn("PLACEHOLDER: notificarCancelacion() - CampanaID: {}, Motivo: {}", idCampana, motivo);
        // TODO: POST /api/v1/{canal}/cancelar/{idCampana}
    }

    @Override
    public void notificarReanudacion(Long idCampana) {
        log.warn("PLACEHOLDER: notificarReanudacion() - CampanaID: {}", idCampana);
        // TODO: POST /api/v1/{canal}/reanudar/{idCampana}
    }
}
