package pe.unmsm.crm.marketing.shared.infra.exception;

public class ExternalServiceException extends BusinessException {

    public ExternalServiceException(String serviceName, String message) {
        super("EXTERNAL_SERVICE_ERROR", "[" + serviceName + "] " + message);
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super("EXTERNAL_SERVICE_ERROR", "[" + serviceName + "] " + message, cause);
    }
}
