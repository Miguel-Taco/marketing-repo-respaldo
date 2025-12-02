package pe.unmsm.crm.marketing.shared.logging;

/**
 * Servicio global de auditoría de negocio.
 * Permite registrar eventos importantes desde cualquier módulo del sistema.
 */
public interface AuditoriaService {

    /**
     * Registra un evento de negocio en el log de auditoría.
     *
     * @param modulo    Módulo funcional donde ocurre el evento
     * @param accion    Acción realizada
     * @param idEntidad Identificador de la entidad afectada (Lead, Segmento,
     *                  Campaña, etc.)
     * @param idUsuario Identificador del usuario que realiza la acción
     * @param detalle   Detalle adicional o descripción del evento
     */
    void registrarEvento(ModuloLog modulo, AccionLog accion, Long idEntidad, Long idUsuario, String detalle);
}
