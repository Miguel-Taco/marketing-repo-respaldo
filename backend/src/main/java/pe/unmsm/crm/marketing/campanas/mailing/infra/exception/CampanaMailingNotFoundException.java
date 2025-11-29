package pe.unmsm.crm.marketing.campanas.mailing.infra.exception;

import lombok.Getter;

@Getter
public class CampanaMailingNotFoundException extends RuntimeException {
    
    private final Integer idCampana;

    public CampanaMailingNotFoundException(Integer idCampana) {
        super("Campaña de mailing con ID " + idCampana + " no encontrada");
        this.idCampana = idCampana;
    }

    public CampanaMailingNotFoundException(String mensaje) {
        super(mensaje);
        this.idCampana = null;
    }
}