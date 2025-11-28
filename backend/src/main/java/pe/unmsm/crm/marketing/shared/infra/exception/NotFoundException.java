package pe.unmsm.crm.marketing.shared.infra.exception;

public class NotFoundException extends BusinessException {

    public NotFoundException(String resourceName, Object id) {
        super(
            "NOT_FOUND",
            resourceName + " con id [" + id + "] no fue encontrado"
        );
    }

    public NotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}
