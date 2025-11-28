package pe.unmsm.crm.marketing.campanas.gestor.domain.model;

/**
 * Enum que representa los tipos de acciones registradas en el historial.
 * Estos valores coinciden con el ENUM de la base de datos.
 */
public enum TipoAccion {
    CREACION,
    EDICION,
    PROGRAMACION,
    REPROGRAMACION,
    ACTIVACION,
    PAUSA,
    REANUDACION,
    CANCELACION,
    FINALIZACION,
    ARCHIVO,
    DUPLICACION,
    ERROR_EJECUCION
}
