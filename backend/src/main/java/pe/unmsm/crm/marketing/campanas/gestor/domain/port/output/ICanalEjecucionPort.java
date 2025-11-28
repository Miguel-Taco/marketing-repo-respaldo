package pe.unmsm.crm.marketing.campanas.gestor.domain.port.output;

import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;

/**
 * Puerto de salida para delegar la ejecución de campanas
 * a los módulos de Mailing o Llamadas.
 */
public interface ICanalEjecucionPort {

    /**
     * Envía una orden de ejecución al canal correspondiente (Mailing o Llamadas).
     * El canal se determina automáticamente según campana.getCanalEjecucion().
     * 
     * @param campana Campaña a ejecutar
     * @return true si la orden fue enviada exitosamente
     */
    boolean ejecutarCampana(Campana campana);

    /**
     * Notifica al canal de ejecución que la campaña ha sido pausada.
     * 
     * @param idCampana ID de la campaña pausada
     * @param motivo    Motivo de la pausa
     */
    void notificarPausa(Long idCampana, String motivo);

    /**
     * Notifica al canal de ejecución que la campaña ha sido cancelada.
     * 
     * @ param idCampana ID de la campaña cancelada
     * 
     * @param motivo Motivo de la cancelación
     */
    void notificarCancelacion(Long idCampana, String motivo);

    /**
     * Notifica al canal de ejecución que la campaña ha sido reanudada.
     * 
     * @param idCampana ID de la campaña reanudada
     */
    void notificarReanudacion(Long idCampana);
}
