package pe.unmsm.crm.marketing.campanas.gestor.api.dto;

/**
 * Enum que representa los estados posibles de una campaña.
 * Estos corresponden a los estados del patrón State implementado en el dominio.
 */
public enum EstadoCampanaEnum {
    BORRADOR("Borrador"),
    PROGRAMADA("Programada"),
    VIGENTE("Vigente"),
    PAUSADA("Pausada"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");

    private final String nombre;

    EstadoCampanaEnum(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    /**
     * Convierte un String a EstadoCampanaEnum
     */
    public static EstadoCampanaEnum fromNombre(String nombre) {
        for (EstadoCampanaEnum estado : values()) {
            if (estado.nombre.equalsIgnoreCase(nombre)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado no válido: " + nombre);
    }
}
