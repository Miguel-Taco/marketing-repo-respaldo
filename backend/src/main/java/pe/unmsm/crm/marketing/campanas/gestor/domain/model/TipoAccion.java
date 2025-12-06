package pe.unmsm.crm.marketing.campanas.gestor.domain.model;

/**
 * Enum que representa los tipos de acciones registradas en el historial.
 * Cada acción tiene una descripción detallada para el usuario.
 */
public enum TipoAccion {
    CREACION("Se creó una nueva campaña en estado Borrador"),
    EDICION("Se actualizaron los datos de la campaña"),
    PROGRAMACION("La campaña fue programada para ejecución automática"),
    REPROGRAMACION("Se modificó la fecha de ejecución programada"),
    ACTIVACION("La campaña pasó a estado Vigente e inició su ejecución"),
    PAUSA("Se pausó la ejecución de la campaña"),
    REANUDACION("Se reanudó la ejecución de la campaña"),
    CANCELACION("La campaña fue cancelada permanentemente"),
    FINALIZACION("La campaña completó su ejecución exitosamente"),
    ARCHIVO("La campaña fue archivada para histórico"),
    DUPLICACION("Se creó un duplicado de esta campaña"),
    ERROR_EJECUCION("Ocurrió un error durante la ejecución de la campaña");

    private final String descripcion;

    TipoAccion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
