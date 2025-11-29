package pe.unmsm.crm.marketing.campanas.mailing.infra.exception;

import lombok.Getter;

@Getter
public class SendGridIntegrationException extends RuntimeException {
    
    private final String tipoError;
    private final Integer codigoError;

    public SendGridIntegrationException(String tipoError, String mensaje) {
        super("Error de SendGrid [" + tipoError + "]: " + mensaje);
        this.tipoError = tipoError;
        this.codigoError = null;
    }

    public SendGridIntegrationException(String tipoError, Integer codigoError, String mensaje) {
        super("Error de SendGrid [" + tipoError + " - " + codigoError + "]: " + mensaje);
        this.tipoError = tipoError;
        this.codigoError = codigoError;
    }

    public SendGridIntegrationException(String mensaje) {
        super("Error de SendGrid: " + mensaje);
        this.tipoError = "UNKNOWN";
        this.codigoError = null;
    }
}