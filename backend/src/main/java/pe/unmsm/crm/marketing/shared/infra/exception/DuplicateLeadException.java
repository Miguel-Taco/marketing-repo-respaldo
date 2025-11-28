package pe.unmsm.crm.marketing.shared.infra.exception;

import lombok.Getter;

/**
 * Excepción lanzada cuando se intenta crear un lead con email o teléfono
 * duplicado.
 */
@Getter
public class DuplicateLeadException extends RuntimeException {

    private final String field; // "email" o "telefono"
    private final String value; // El valor duplicado

    public DuplicateLeadException(String field, String value) {
        super(String.format("Ya existe un lead con %s: %s", field, value));
        this.field = field;
        this.value = value;
    }
}
