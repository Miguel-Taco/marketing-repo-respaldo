package pe.unmsm.crm.marketing.campanas.gestor.domain.port.output;

import java.time.LocalDateTime;

/**
 * Puerto de salida para consultar la disponibilidad de recursos externos
 * (Agente, Segmento, Encuesta) antes de programar una campaña.
 */
public interface IConsultaRecursosPort {

    /**
     * Verifica si un segmento existe, está activo y tiene miembros.
     * 
     * @param idSegmento ID del segmento a validar
     * @return true si el segmento es válido para usar en una campaña
     */
    boolean existeSegmento(Long idSegmento);

    /**
     * Verifica si un agente está disponible en el rango de fechas especificado.
     * 
     * @param idAgente    ID del agente
     * @param fechaInicio Fecha de inicio de la campaña
     * @param fechaFin    Fecha de fin de la campaña
     * @return true si el agente está disponible
     */
    boolean isAgenteDisponible(Integer idAgente, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Verifica si una encuesta existe y está activa.
     * 
     * @param idEncuesta ID de la encuesta
     * @return true si la encuesta está disponible
     */
    boolean existeEncuesta(Integer idEncuesta);
}
