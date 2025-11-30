package pe.unmsm.crm.marketing.campanas.gestor.domain.port.output;

import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;

/**
 * Puerto de salida para delegar la ejecución de campanas
 * a los módulos de Mailing o Llamadas.
 */
public interface ICanalEjecucionPort {

    /**
     * Envía la orden de programación inicial al canal.
     * Crea la campaña en el módulo destino en estado PENDIENTE/PROGRAMADA.
     * 
     * @param campana Campaña a programar
     */
    void programarCampana(Campana campana);

    /**
     * Activa la campaña en el canal de ejecución.
     * Cambia el estado a LISTO/VIGENTE para iniciar el procesamiento.
     * 
     * @param campana Campaña a activar
     * @return true si la activación fue exitosa
     */
    boolean activarCampana(Campana campana);

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
     * @param idCampana ID de la campaña cancelada
     * @param motivo    Motivo de la cancelación
     */
    void notificarCancelacion(Long idCampana, String motivo);

    /**
     * Notifica al canal de ejecución que la campaña ha sido reanudada.
     * 
     * @param idCampana ID de la campaña reanudada
     */
    void notificarReanudacion(Long idCampana);

    /**
     * Notifica al canal de ejecución que la campaña ha sido reprogramada.
     * 
     * @param campana Campaña con las nuevas fechas
     */
    void reprogramarCampana(Campana campana);
}
