package pe.unmsm.crm.marketing.campanas.mailing.infra.exception;

import lombok.Getter;

@Getter
public class CampanaMailingInvalidStateException extends RuntimeException {
    
    private final String estadoActual;
    private final String estadoEsperado;

    public CampanaMailingInvalidStateException(String estadoActual, String estadoEsperado) {
        super(String.format("Estado inválido. Actual: %s, Esperado: %s", estadoActual, estadoEsperado));
        this.estadoActual = estadoActual;
        this.estadoEsperado = estadoEsperado;
    }

    public CampanaMailingInvalidStateException(String mensaje) {
        super(mensaje);
        this.estadoActual = null;
        this.estadoEsperado = null;
    }
}