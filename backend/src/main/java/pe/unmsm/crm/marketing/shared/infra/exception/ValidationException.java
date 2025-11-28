package pe.unmsm.crm.marketing.shared.infra.exception;

public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }

    public ValidationException(String code, String message) {
        super(code, message);
    }
}
