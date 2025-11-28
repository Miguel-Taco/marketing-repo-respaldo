package pe.unmsm.crm.marketing.campanas.gestor.domain.exception;

/**
 * Excepción lanzada cuando se intenta realizar una transición de estado no
 * permitida.
 */
public class EstadoIlegalException extends RuntimeException {

    public EstadoIlegalException(String mensaje) {
        super(mensaje);
    }

    public EstadoIlegalException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
